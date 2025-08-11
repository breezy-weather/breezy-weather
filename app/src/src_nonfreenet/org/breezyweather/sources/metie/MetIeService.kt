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

package org.breezyweather.sources.metie

import android.content.Context
import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.location.model.LocationAddressInfo
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.AlertSeverity
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.extensions.toDateNoHour
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.sources.metie.json.MetIeHourly
import org.breezyweather.sources.metie.json.MetIeLocationResult
import org.breezyweather.sources.metie.json.MetIeWarning
import org.breezyweather.sources.metie.json.MetIeWarningResult
import org.breezyweather.unit.pressure.Pressure.Companion.hectopascals
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Named

/**
 * MET Éireann service
 */
class MetIeService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ReverseGeocodingSource, LocationParametersSource {

    override val id = "metie"
    private val countryName = context.currentLocale.getCountryName("IE")
    override val name = "MET Éireann".let {
        if (it.contains(countryName)) {
            it
        } else {
            "$it ($countryName)"
        }
    }
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl = "https://www.met.ie/about-us/privacy"

    private val mApi by lazy {
        client
            .baseUrl(MET_IE_BASE_URL)
            .build()
            .create(MetIeApi::class.java)
    }

    // Terms require: copyright + source + license (with link) + disclaimer + mention of modified data
    private val weatherAttribution = "Copyright Met Éireann. Source met.ie. This data is published under a " +
        "Commons Attribution 4.0 International (CC BY 4.0). Met Éireann does not accept any liability whatsoever " +
        "for any error or omission in the data, their availability, or for any loss or damage arising from their " +
        "use. ${context.getString(R.string.data_modified, context.getString(R.string.breezy_weather))}"
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution,
        SourceFeature.REVERSE_GEOCODING to weatherAttribution
    )
    override val attributionLinks = mapOf(
        "Met Éireann" to "https://www.met.ie/",
        "met.ie" to "https://www.met.ie/",
        "Creative Commons Attribution 4.0 International (CC BY 4.0)" to "https://creativecommons.org/licenses/by/4.0/"
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return location.countryCode.equals("IE", ignoreCase = true)
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
        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()
        val forecast = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getForecast(
                location.latitude,
                location.longitude
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }
        val alerts = if (SourceFeature.ALERT in requestedFeatures) {
            mApi.getWarnings().onErrorResumeNext {
                failedFeatures[SourceFeature.ALERT] = it
                Observable.just(MetIeWarningResult())
            }
        } else {
            Observable.just(MetIeWarningResult())
        }

        return Observable.zip(forecast, alerts) { forecastResult: List<MetIeHourly>, alertsResult: MetIeWarningResult ->
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(location, forecastResult)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(forecastResult)
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlertList(location, alertsResult.warnings?.national)
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    private fun getDailyForecast(
        location: Location,
        hourlyResult: List<MetIeHourly>,
    ): List<DailyWrapper> {
        val dailyList = mutableListOf<DailyWrapper>()
        val hourlyListByDay = hourlyResult.groupBy { it.date }
        for (i in 0 until hourlyListByDay.entries.size - 1) {
            val dayDate = hourlyListByDay.keys.toTypedArray()[i].toDateNoHour(location.timeZone)
            if (dayDate != null) {
                dailyList.add(
                    DailyWrapper(
                        date = dayDate
                    )
                )
            }
        }
        return dailyList
    }

    /**
     * Returns hourly forecast
     */
    private fun getHourlyForecast(
        hourlyResult: List<MetIeHourly>,
    ): List<HourlyWrapper> {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getTimeZone("Europe/Dublin")

        return hourlyResult.map { result ->
            HourlyWrapper(
                date = formatter.parse("${result.date} ${result.time}")!!,
                weatherCode = getWeatherCode(result.weatherNumber),
                weatherText = result.weatherDescription,
                temperature = TemperatureWrapper(
                    temperature = result.temperature?.toDouble()
                ),
                precipitation = Precipitation(
                    total = result.rainfall?.toDoubleOrNull()
                ),
                wind = Wind(
                    degree = result.windDirection?.toDoubleOrNull(),
                    speed = result.windSpeed?.div(3.6)
                ),
                relativeHumidity = result.humidity?.toDoubleOrNull(),
                pressure = result.pressure?.toDoubleOrNull()?.hectopascals
            )
        }
    }

    private fun getAlertList(location: Location, warnings: List<MetIeWarning>?): List<Alert>? {
        if (warnings == null) return null
        if (warnings.isEmpty()) return emptyList()

        val region = if (MetIeService.regionsMapping.containsKey(location.admin2)) {
            location.admin2
        } else {
            location.parameters.getOrElse("metie") { null }?.getOrElse("region") { null }
        }
        val eiRegion = region?.let { MetIeService.regionsMapping.getOrElse(region) { null } }

        return warnings
            .filter {
                // National
                it.regions.contains("EI0") ||
                    it.regions.contains(eiRegion)
            }
            .map { alert ->
                val severity = when (alert.severity?.lowercase()) {
                    "extreme" -> AlertSeverity.EXTREME
                    "severe" -> AlertSeverity.SEVERE
                    "moderate" -> AlertSeverity.MODERATE
                    "minor" -> AlertSeverity.MINOR
                    else -> AlertSeverity.UNKNOWN
                }
                Alert(
                    alertId = alert.id,
                    startDate = alert.onset,
                    endDate = alert.expiry,
                    headline = alert.headline,
                    description = alert.description,
                    source = "MET Éireann",
                    severity = severity,
                    color = when (alert.level?.lowercase()) {
                        "red" -> Color.rgb(224, 0, 0)
                        "orange" -> Color.rgb(255, 140, 0)
                        "yellow" -> Color.rgb(255, 255, 0)
                        else -> Alert.colorFromSeverity(severity)
                    }
                )
            }
    }

    private fun getWeatherCode(icon: String?): WeatherCode? {
        if (icon == null) return null
        return with(icon) {
            when {
                startsWith("01") ||
                    startsWith("02") -> WeatherCode.CLEAR
                startsWith("03") -> WeatherCode.PARTLY_CLOUDY
                startsWith("04") -> WeatherCode.CLOUDY
                startsWith("05") ||
                    startsWith("09") ||
                    startsWith("10") ||
                    startsWith("40") ||
                    startsWith("41") ||
                    startsWith("46") -> WeatherCode.RAIN
                startsWith("06") ||
                    startsWith("11") ||
                    startsWith("14") ||
                    startsWith("2") ||
                    startsWith("30") ||
                    startsWith("31") ||
                    startsWith("32") ||
                    startsWith("33") ||
                    startsWith("34") -> WeatherCode.THUNDERSTORM
                startsWith("07") ||
                    startsWith("12") ||
                    startsWith("42") ||
                    startsWith("43") ||
                    startsWith("47") ||
                    startsWith("48") -> WeatherCode.SLEET
                startsWith("08") ||
                    startsWith("13") ||
                    startsWith("44") ||
                    startsWith("45") ||
                    startsWith("49") ||
                    startsWith("50") -> WeatherCode.SNOW
                startsWith("15") -> WeatherCode.FOG
                startsWith("51") ||
                    startsWith("52") -> WeatherCode.HAIL
                else -> null
            }
        }
    }

    override fun requestNearestLocation(
        context: Context,
        latitude: Double,
        longitude: Double,
    ): Observable<List<LocationAddressInfo>> {
        return mApi.getReverseLocation(
            latitude,
            longitude
        ).map {
            val locationList = mutableListOf<LocationAddressInfo>()
            if (it.city != "NO LOCATION SELECTED") {
                locationList.add(convertLocation(it))
            }
            locationList
        }
    }

    private fun convertLocation(
        result: MetIeLocationResult,
    ): LocationAddressInfo {
        return LocationAddressInfo(
            timeZoneId = "Europe/Dublin",
            countryCode = "IE",
            admin2 = result.county,
            city = result.city
        )
    }

    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        if (regionsMapping.containsKey(location.admin2)) return false

        val currentRegion = location.parameters
            .getOrElse(id) { null }?.getOrElse("region") { null }

        return currentRegion.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        return mApi.getReverseLocation(
            location.latitude,
            location.longitude
        ).map {
            if (it.city != "NO LOCATION SELECTED" &&
                !it.county.isNullOrEmpty() &&
                regionsMapping.containsKey(it.county)
            ) {
                mapOf("region" to it.county)
            } else {
                throw InvalidLocationException()
            }
        }
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val MET_IE_BASE_URL = "https://prodapi.metweb.ie/"

        // Last checked: 2024-03-02 https://prodapi.metweb.ie/v2/warnings/regions
        val regionsMapping = mapOf(
            "All Counties" to "EI0",
            "All Sea Areas" to "EI8",
            "Carlow" to "EI01",
            "Cavan" to "EI02",
            "Clare" to "EI03",
            "Cork" to "EI04",
            "Donegal" to "EI06",
            "Dublin" to "EI07",
            "Galway" to "EI10",
            "Kerry" to "EI11",
            "Kildare" to "EI12",
            "Kilkenny" to "EI13",
            "Laois" to "EI15",
            "Leitrim" to "EI14",
            "Limerick" to "EI16",
            "Longford" to "EI18",
            "Louth" to "EI19",
            "Mayo" to "EI20",
            "Meath" to "EI21",
            "Monaghan" to "EI22",
            "Offaly" to "EI23",
            "Roscommon" to "EI24",
            "Sligo" to "EI25",
            "Tipperary" to "EI26",
            "Waterford" to "EI27",
            "Westmeath" to "EI29",
            "Wexford" to "EI30",
            "Wicklow" to "EI31"
        )
    }
}
