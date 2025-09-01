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
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCalendarMonth
import org.breezyweather.common.extensions.toDate
import org.breezyweather.common.extensions.toTimezoneNoHour
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.domain.settings.SourceConfigStore
import org.breezyweather.sources.eccc.json.EcccAlert
import org.breezyweather.sources.eccc.json.EcccDailyFcst
import org.breezyweather.sources.eccc.json.EcccHourly
import org.breezyweather.sources.eccc.json.EcccObservation
import org.breezyweather.sources.eccc.json.EcccRegionalNormalsMetric
import org.breezyweather.sources.eccc.json.EcccResult
import org.breezyweather.sources.eccc.json.EcccUnit
import org.breezyweather.sources.getWindDegree
import org.breezyweather.unit.distance.Distance.Companion.kilometers
import org.breezyweather.unit.pressure.Pressure.Companion.kilopascals
import org.breezyweather.unit.ratio.Ratio.Companion.percent
import org.breezyweather.unit.speed.Speed.Companion.kilometersPerHour
import org.breezyweather.unit.temperature.Temperature.Companion.celsius
import retrofit2.Retrofit
import java.util.Calendar
import java.util.Date
import java.util.Objects
import javax.inject.Inject
import javax.inject.Named
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

class EcccService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : EcccServiceStub(context) {

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

    override val attributionLinks
        get() = mapOf(
            "Environnement et Changement Climatique Canada" to "https://meteo.gc.ca/",
            "Environment and Climate Change Canada" to "https://weather.gc.ca/"
        )

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val languageCode = if (context.currentLocale.language.startsWith("fr", ignoreCase = true)) "fr" else "en"

        return mApi.getForecast(
            userAgent = USER_AGENT,
            apiKey = getApiKeyOrDefault(),
            lang = languageCode,
            lat = location.latitude,
            lon = location.longitude
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
                temperature = getNonEmptyMetric(result.temperature)?.celsius,
                feelsLike = getNonEmptyMetric(result.feelsLike)?.celsius
            ),
            wind = Wind(
                degree = result.windBearing?.toDoubleOrNull(),
                speed = getNonEmptyMetric(result.windSpeed)?.kilometersPerHour,
                gusts = getNonEmptyMetric(result.windGust)?.kilometersPerHour
            ),
            relativeHumidity = result.humidity?.toDoubleOrNull()?.percent,
            dewPoint = getNonEmptyMetric(result.dewpoint)?.celsius,
            pressure = getNonEmptyMetric(result.pressure)?.kilopascals,
            visibility = getNonEmptyMetric(result.visibility)?.kilometers
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
                                        temperature = daytime.temperature?.periodHigh?.celsius
                                    ),
                                    precipitationProbability = PrecipitationProbability(
                                        total = daytime.precip?.toDoubleOrNull()?.percent
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
                                    temperature = nighttime.temperature?.periodLow?.celsius
                                ),
                                precipitationProbability = PrecipitationProbability(
                                    total = nighttime.precip?.toDoubleOrNull()?.percent
                                )
                            ),
                            sunshineDuration = daytime?.sun?.value?.toDoubleOrNull()?.hours
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
                    temperature = getNonEmptyMetric(result.temperature)?.celsius,
                    feelsLike = getNonEmptyMetric(result.feelsLike)?.celsius
                ),
                precipitationProbability = if (!result.precip.isNullOrEmpty()) {
                    PrecipitationProbability(total = result.precip.toDoubleOrNull()?.percent)
                } else {
                    null
                },
                wind = Wind(
                    degree = getWindDegree(result.windDir),
                    speed = getNonEmptyMetric(result.windSpeed)?.kilometersPerHour,
                    gusts = getNonEmptyMetric(result.windGust)?.kilometersPerHour
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
            daytimeTemperature = normals.highTemp.celsius,
            nighttimeTemperature = normals.lowTemp.celsius
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
        val languageCode = if (context.currentLocale.language.startsWith("fr", ignoreCase = true)) "fr" else "en"

        return mApi.getForecast(
            userAgent = USER_AGENT,
            apiKey = getApiKeyOrDefault(),
            lang = languageCode,
            lat = latitude,
            lon = longitude
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

    // CONFIG
    private val config = SourceConfigStore(context, id)
    private var apikey: String
        set(value) {
            config.edit().putString("apikey", value).apply()
        }
        get() = config.getString("apikey", null) ?: ""

    private fun getApiKeyOrDefault(): String {
        return apikey.ifEmpty { BuildConfig.ECCC_KEY }
    }
    override val isConfigured
        get() = getApiKeyOrDefault().isNotEmpty()

    override val isRestricted = false

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_weather_source_eccc_api_key,
                summary = { c, content ->
                    content.ifEmpty {
                        c.getString(R.string.settings_source_default_value)
                    }
                },
                content = apikey,
                onValueChanged = {
                    apikey = it
                }
            )
        )
    }

    companion object {
        private const val ECCC_BASE_URL = "https://app.weather.gc.ca/"
        // Most sold device in Canada
        private const val USER_AGENT = "WeatherAppAndroid 2.3.0build199;14;moto g 5G - 2024"
    }
}
