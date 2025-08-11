/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.sources.eccc

import android.content.Context
import androidx.core.graphics.toColorInt
import breezyweather.domain.location.model.Location
import breezyweather.domain.location.model.LocationAddressInfo
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.Normals
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.AlertSeverity
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HalfDayWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCalendarMonth
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.extensions.toDate
import org.breezyweather.common.extensions.toTimezoneNoHour
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.sources.eccc.json.EcccAlert
import org.breezyweather.sources.eccc.json.EcccDailyFcst
import org.breezyweather.sources.eccc.json.EcccHourly
import org.breezyweather.sources.eccc.json.EcccObservation
import org.breezyweather.sources.eccc.json.EcccRegionalNormalsMetric
import org.breezyweather.sources.eccc.json.EcccResult
import org.breezyweather.sources.eccc.json.EcccUnit
import org.breezyweather.sources.getWindDegree
import org.breezyweather.unit.pressure.Pressure.Companion.kilopascals
import retrofit2.Retrofit
import java.util.Calendar
import java.util.Date
import java.util.Objects
import javax.inject.Inject
import javax.inject.Named
import kotlin.time.Duration.Companion.seconds

class EcccService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ReverseGeocodingSource {

    override val id = "eccc"
    override val name = "ECCC (${context.currentLocale.getCountryName("CA")})"
    override val continent = SourceContinent.NORTH_AMERICA
    override val privacyPolicyUrl by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("fr") -> "https://app.weather.gc.ca/privacy-fr.html"
                else -> "https://app.weather.gc.ca/privacy-en.html"
            }
        }
    }

    private val mApi by lazy {
        client
            .baseUrl(ECCC_BASE_URL)
            .build()
            .create(EcccApi::class.java)
    }

    private val weatherAttribution by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("fr") ->
                    "Environnement et Changement Climatique Canada (Licence d’utilisation finale" +
                        " pour les serveurs de données d’Environnement et Changement Climatique Canada)"
                else ->
                    "Environment and Climate Change Canada" +
                        " (Environment and Climate Change Canada Data Servers End-use Licence)"
            }
        }
    }
    override val attributionLinks
        get() = mapOf(
            "Environnement et Changement Climatique Canada" to "https://meteo.gc.ca/",
            "Environment and Climate Change Canada" to "https://weather.gc.ca/"
        )
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution,
        SourceFeature.NORMALS to weatherAttribution,
        SourceFeature.REVERSE_GEOCODING to weatherAttribution
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return location.countryCode.equals("CA", ignoreCase = true)
    }

    override fun getFeaturePriorityForLocation(
        location: Location,
        feature: SourceFeature,
    ): Int {
        return when {
            isFeatureSupportedForLocation(location, feature) -> PRIORITY_HIGHEST
            else -> PRIORITY_NONE
        }
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        return mApi.getForecast(
            context.currentLocale.code,
            location.latitude,
            location.longitude
        ).map {
            // Can’t do that because it is a List when it succeed
            // if (it.error == "OUT_OF_SERVICE_BOUNDARY") {
            if (it.isEmpty()) {
                throw InvalidLocationException()
            }

            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(location, it[0].dailyFcst)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(it[0].hourlyFcst?.hourly)
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(it[0].observation)
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlertList(it[0].alert?.alerts)
                } else {
                    null
                },
                normals = if (SourceFeature.NORMALS in requestedFeatures) {
                    getNormals(it[0].dailyFcst?.regionalNormals?.metric)?.let { normals ->
                        mapOf(
                            Date().getCalendarMonth(location) to normals
                        )
                    }
                } else {
                    null
                }
            )
        }
    }

    /**
     * Returns current weather
     */
    private fun getCurrent(result: EcccObservation?): CurrentWrapper? {
        if (result == null) return null
        return CurrentWrapper(
            weatherCode = getWeatherCode(result.iconCode),
            weatherText = result.condition,
            temperature = TemperatureWrapper(
                temperature = getNonEmptyMetric(result.temperature),
                feelsLike = getNonEmptyMetric(result.feelsLike)
            ),
            wind = Wind(
                degree = result.windBearing?.toDoubleOrNull(),
                speed = getNonEmptyMetric(result.windSpeed)?.div(3.6),
                gusts = getNonEmptyMetric(result.windGust)?.div(3.6)
            ),
            relativeHumidity = result.humidity?.toDoubleOrNull(),
            dewPoint = getNonEmptyMetric(result.dewpoint),
            pressure = getNonEmptyMetric(result.pressure)?.kilopascals,
            visibility = getNonEmptyMetric(result.visibility)?.times(1000)
        )
    }

    /**
     * Returns daily forecast
     * The daily list is actually a list of daytime/nighttime periods starting from dailyIssuedTime,
     * making the parsing a bit more complex than other sources
     */
    private fun getDailyForecast(
        location: Location,
        dailyResult: EcccDailyFcst?,
    ): List<DailyWrapper> {
        if (dailyResult == null) return emptyList()
        val dailyFirstDay = dailyResult.dailyIssuedTimeEpoch!!.toLong().seconds.inWholeMilliseconds.toDate()
            .toTimezoneNoHour(location.timeZone)
        val dailyList = mutableListOf<DailyWrapper>()
        if (dailyFirstDay != null) {
            val firstDayIsNight = dailyResult.daily!![0].temperature?.periodLow != null
            for (i in 0 until 6) {
                val daytime = if (!firstDayIsNight) {
                    dailyResult.daily.getOrNull(i * 2)
                } else {
                    if (i != 0) {
                        dailyResult.daily.getOrNull((i * 2) - 1)
                    } else {
                        null
                    }
                }
                val nighttime = if (!firstDayIsNight) {
                    dailyResult.daily.getOrNull((i * 2) + 1)
                } else {
                    dailyResult.daily.getOrNull(i * 2)
                }

                if ((daytime != null && nighttime != null) || (firstDayIsNight && i == 0 && nighttime != null)) {
                    val currentDay = if (i != 0) {
                        val cal = Calendar.getInstance()
                        cal.setTime(dailyFirstDay)
                        cal.add(Calendar.DAY_OF_YEAR, i)
                        cal.time
                    } else {
                        dailyFirstDay
                    }

                    dailyList.add(
                        DailyWrapper(
                            date = currentDay,
                            day = if (daytime != null) {
                                HalfDayWrapper(
                                    weatherCode = getWeatherCode(daytime.iconCode),
                                    weatherText = daytime.summary,
                                    weatherSummary = daytime.text,
                                    temperature = TemperatureWrapper(
                                        temperature = daytime.temperature?.periodHigh?.toDouble()
                                    ),
                                    precipitationProbability = PrecipitationProbability(
                                        total = daytime.precip?.toDoubleOrNull()
                                    )
                                )
                            } else {
                                null
                            },
                            night = HalfDayWrapper(
                                weatherCode = getWeatherCode(nighttime.iconCode),
                                weatherText = nighttime.summary,
                                weatherSummary = nighttime.text,
                                temperature = TemperatureWrapper(
                                    temperature = nighttime.temperature?.periodLow?.toDouble()
                                ),
                                precipitationProbability = PrecipitationProbability(
                                    total = nighttime.precip?.toDoubleOrNull()
                                )
                            ),
                            sunshineDuration = daytime?.sun?.value?.toDoubleOrNull()
                        )
                    )
                }
            }
        }
        return dailyList
    }

    /**
     * Returns hourly forecast
     */
    private fun getHourlyForecast(
        hourlyResult: List<EcccHourly>?,
    ): List<HourlyWrapper> {
        if (hourlyResult.isNullOrEmpty()) return emptyList()
        return hourlyResult.map { result ->
            HourlyWrapper(
                date = result.epochTime.times(1000L).toDate(),
                weatherText = result.condition,
                weatherCode = getWeatherCode(result.iconCode),
                temperature = TemperatureWrapper(
                    temperature = getNonEmptyMetric(result.temperature),
                    feelsLike = getNonEmptyMetric(result.feelsLike)
                ),
                precipitationProbability = if (!result.precip.isNullOrEmpty()) {
                    PrecipitationProbability(total = result.precip.toDoubleOrNull())
                } else {
                    null
                },
                wind = Wind(
                    degree = getWindDegree(result.windDir),
                    speed = getNonEmptyMetric(result.windSpeed)?.div(3.6),
                    gusts = getNonEmptyMetric(result.windGust)?.div(3.6)
                ),
                uV = if (!result.uv?.index.isNullOrEmpty()) {
                    UV(index = result.uv.index.toDoubleOrNull())
                } else {
                    null
                }
            )
        }
    }

    /**
     * Returns alerts
     */
    private fun getAlertList(alertList: List<EcccAlert>?): List<Alert>? {
        if (alertList.isNullOrEmpty()) return null
        return alertList.map { alert ->
            val severity = when (alert.type) {
                "warning" -> AlertSeverity.SEVERE
                "watch" -> AlertSeverity.MODERATE
                "statement" -> AlertSeverity.MINOR
                else -> AlertSeverity.UNKNOWN
            }
            Alert(
                alertId = alert.alertId ?: Objects.hash(alert.alertBannerText, alert.issueTime).toString(),
                startDate = alert.issueTime,
                endDate = alert.expiryTime,
                headline = alert.alertBannerText,
                description = alert.text,
                source = alert.specialText?.firstOrNull { it.type == "email" }?.link,
                severity = severity,
                color = (if (alert.bannerColour?.startsWith("#") == true) alert.bannerColour.toColorInt() else null)
                    ?: Alert.colorFromSeverity(severity)
            )
        }
    }

    /**
     * Returns normals
     */
    private fun getNormals(normals: EcccRegionalNormalsMetric?): Normals? {
        if (normals?.highTemp == null || normals.lowTemp == null) return null
        return Normals(
            daytimeTemperature = normals.highTemp.toDouble(),
            nighttimeTemperature = normals.lowTemp.toDouble()
        )
    }

    private fun getNonEmptyMetric(ecccUnit: EcccUnit?): Double? {
        if (ecccUnit == null || (ecccUnit.metric.isNullOrEmpty() && ecccUnit.metricUnrounded.isNullOrEmpty())) {
            return null
        }
        return if (!ecccUnit.metricUnrounded.isNullOrEmpty()) {
            ecccUnit.metricUnrounded.toDoubleOrNull()
        } else {
            ecccUnit.metric!!.toDoubleOrNull()
        }
    }

    private fun getWeatherCode(icon: String?): WeatherCode? {
        return when (icon) {
            "00", "01", "30", "31" -> WeatherCode.CLEAR
            "02", "03", "04", "05", "22", "32", "33", "34", "35" -> WeatherCode.PARTLY_CLOUDY
            "10", "20", "21" -> WeatherCode.CLOUDY
            "06", "11", "12", "13", "28", "36" -> WeatherCode.RAIN
            "14", "27" -> WeatherCode.HAIL
            "07", "15", "37" -> WeatherCode.SLEET
            "08", "16", "17", "18", "26", "38" -> WeatherCode.SNOW
            "09", "19", "39", "46", "47" -> WeatherCode.THUNDERSTORM
            "23", "44", "45" -> WeatherCode.HAZE
            "24" -> WeatherCode.FOG
            "25", "40", "41", "42", "43", "48" -> WeatherCode.WIND
            else -> null
        }
    }

    override fun requestNearestLocation(
        context: Context,
        latitude: Double,
        longitude: Double,
    ): Observable<List<LocationAddressInfo>> {
        return mApi.getForecast(
            context.currentLocale.code,
            latitude,
            longitude
        ).map {
            if (it.isEmpty()) {
                throw InvalidLocationException()
            }
            listOf(convertLocation(it[0]))
        }
    }

    private fun convertLocation(
        result: EcccResult,
    ): LocationAddressInfo {
        return LocationAddressInfo(
            countryCode = "CA",
            city = result.displayName
        )
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val ECCC_BASE_URL = "https://app.weather.gc.ca/"
    }
}
