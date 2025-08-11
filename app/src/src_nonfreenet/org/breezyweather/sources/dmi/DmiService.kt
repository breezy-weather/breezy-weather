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

package org.breezyweather.sources.dmi

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
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.extensions.getIsoFormattedDate
import org.breezyweather.common.extensions.toDateNoHour
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.sources.dmi.json.DmiResult
import org.breezyweather.sources.dmi.json.DmiTimeserie
import org.breezyweather.sources.dmi.json.DmiWarning
import org.breezyweather.sources.dmi.json.DmiWarningResult
import org.breezyweather.unit.pressure.Pressure.Companion.hectopascals
import retrofit2.Retrofit
import java.util.Objects
import javax.inject.Inject
import javax.inject.Named

class DmiService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ReverseGeocodingSource, LocationParametersSource {

    override val id = "dmi"
    override val name = "DMI (${context.currentLocale.getCountryName("DK")})"
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl = "https://www.dmi.dk/om-hjemmesiden/privatliv/"

    private val mApi by lazy {
        client
            .baseUrl(DMI_BASE_URL)
            .build()
            .create(DmiApi::class.java)
    }

    private val weatherAttribution = "DMI (Creative Commons CC BY)"
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution,
        SourceFeature.REVERSE_GEOCODING to weatherAttribution
    )
    override val attributionLinks = mapOf(
        "DMI" to "https://www.dmi.dk/"
    )
    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return feature != SourceFeature.ALERT ||
            arrayOf("DK", "FO", "GL").any { it.equals(location.countryCode, ignoreCase = true) }
    }

    override fun getFeaturePriorityForLocation(
        location: Location,
        feature: SourceFeature,
    ): Int {
        return when {
            // Always use the same criterias as alert
            isFeatureSupportedForLocation(location, SourceFeature.ALERT) -> PRIORITY_HIGHEST
            else -> PRIORITY_NONE
        }
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()
        val weather = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getWeather(
                location.latitude,
                location.longitude,
                DMI_WEATHER_CMD
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(DmiResult())
            }
        } else {
            Observable.just(DmiResult())
        }
        val alerts = if (SourceFeature.ALERT in requestedFeatures) {
            val id = location.parameters.getOrElse(id) { null }?.getOrElse("id") { null }
            if (!id.isNullOrEmpty()) {
                mApi.getAlerts(id).onErrorResumeNext {
                    failedFeatures[SourceFeature.ALERT] = it
                    Observable.just(DmiWarningResult())
                }
            } else {
                failedFeatures[SourceFeature.ALERT] = InvalidLocationException()
                Observable.just(DmiWarningResult())
            }
        } else {
            Observable.just(DmiWarningResult())
        }

        return Observable.zip(
            weather,
            alerts
        ) { weatherResult: DmiResult, alertsResult: DmiWarningResult ->
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(location, weatherResult.timeserie)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(weatherResult.timeserie)
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlertList(alertsResult.locationWarnings)
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    /**
     * Returns empty daily forecast
     * Will be completed with hourly forecast data later
     */
    private fun getDailyForecast(
        location: Location,
        dailyResult: List<DmiTimeserie>?,
    ): List<DailyWrapper> {
        if (dailyResult.isNullOrEmpty()) return emptyList()

        val dailyList = mutableListOf<DailyWrapper>()
        val hourlyListByDay = dailyResult.groupBy {
            it.localTimeIso.getIsoFormattedDate(location)
        }
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
        hourlyResult: List<DmiTimeserie>?,
    ): List<HourlyWrapper> {
        if (hourlyResult.isNullOrEmpty()) return emptyList()

        return hourlyResult.map { result ->
            HourlyWrapper(
                // TODO: Check units
                date = result.localTimeIso,
                weatherCode = getWeatherCode(result.symbol),
                temperature = TemperatureWrapper(
                    temperature = result.temp
                ),
                precipitation = if (result.precip != null) {
                    Precipitation(total = result.precip)
                } else {
                    null
                },
                wind = Wind(
                    degree = result.windDegree,
                    speed = result.windSpeed,
                    gusts = result.windGust
                ),
                relativeHumidity = result.humidity,
                pressure = result.pressure?.hectopascals,
                visibility = result.visibility
            )
        }
    }

    private fun getAlertList(
        resultList: List<DmiWarning>?,
    ): List<Alert>? {
        if (resultList.isNullOrEmpty()) return null
        return resultList.map {
            Alert(
                alertId = Objects.hash(it.warningTitle, it.validFrom).toString(),
                startDate = it.validFrom,
                endDate = it.validTo,
                headline = it.warningTitle,
                description = it.warningText,
                instruction = it.additionalText,
                source = "DMI",
                severity = when (it.formattedCategory) {
                    3 -> AlertSeverity.EXTREME
                    2 -> AlertSeverity.SEVERE
                    1 -> AlertSeverity.MODERATE
                    0 -> AlertSeverity.MINOR
                    else -> AlertSeverity.UNKNOWN
                },
                color = when (it.formattedCategory) {
                    3 -> Color.rgb(204, 31, 31)
                    2 -> Color.rgb(254, 142, 82)
                    1 -> Color.rgb(255, 217, 3)
                    0 -> Color.rgb(146, 208, 245)
                    else -> Alert.colorFromSeverity(AlertSeverity.UNKNOWN)
                }
            )
        }
    }

    private fun getWeatherCode(icon: Int?): WeatherCode? {
        return when (icon) {
            1, 101 -> WeatherCode.CLEAR
            2, 102 -> WeatherCode.PARTLY_CLOUDY
            3, 103 -> WeatherCode.CLOUDY
            60, 63, 80, 160, 163, 180, 181 -> WeatherCode.RAIN
            168, 169, 183, 184 -> WeatherCode.SLEET
            70, 138, 170, 173, 185, 186 -> WeatherCode.SNOW
            195 -> WeatherCode.THUNDERSTORM
            145 -> WeatherCode.FOG
            else -> null
        }
    }

    // Reverse geocoding
    override fun requestNearestLocation(
        context: Context,
        latitude: Double,
        longitude: Double,
    ): Observable<List<LocationAddressInfo>> {
        return mApi.getWeather(
            latitude,
            longitude,
            DMI_WEATHER_CMD
        ).map {
            listOf(convertLocation(it))
        }
    }

    private fun convertLocation(
        result: DmiResult,
    ): LocationAddressInfo {
        if (result.country.isNullOrEmpty()) {
            throw InvalidLocationException()
        }

        return LocationAddressInfo(
            timeZoneId = result.timezone,
            countryCode = result.country,
            city = result.city,
            cityCode = result.id
        )
    }

    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        // If we are in Denmark, Faroe Islands, or Greenland, we need location parameters
        // (update it if coordinates changes, OR if we didn't have it yet)
        return SourceFeature.ALERT in features &&
            (coordinatesChanged || location.parameters.getOrElse(id) { null }?.getOrElse("id") { null }.isNullOrEmpty())
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        return mApi.getWeather(
            location.latitude,
            location.longitude,
            DMI_WEATHER_CMD
        ).map {
            if (it.id.isNullOrEmpty()) {
                throw InvalidLocationException()
            }
            mapOf("id" to it.id)
        }
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val DMI_BASE_URL = "https://www.dmi.dk/"
        private const val DMI_WEATHER_CMD = "llj"
    }
}
