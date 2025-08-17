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

package org.breezyweather.sources.geosphereat

import android.content.Context
import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.Minutely
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.AlertSeverity
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.AirQualityWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import com.google.maps.android.model.LatLng
import com.google.maps.android.model.LatLngBounds
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.extensions.getIsoFormattedDate
import org.breezyweather.common.extensions.minus
import org.breezyweather.common.extensions.toDate
import org.breezyweather.common.extensions.toDateNoHour
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGH
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.sources.geosphereat.json.GeoSphereAtTimeseriesResult
import org.breezyweather.sources.geosphereat.json.GeoSphereAtWarningsResult
import org.breezyweather.unit.pollutant.PollutantConcentration.Companion.microgramsPerCubicMeter
import org.breezyweather.unit.precipitation.Precipitation.Companion.millimeters
import org.breezyweather.unit.pressure.Pressure.Companion.pascals
import org.breezyweather.unit.ratio.Ratio.Companion.fraction
import org.breezyweather.unit.ratio.Ratio.Companion.percent
import org.breezyweather.unit.speed.Speed.Companion.metersPerSecond
import org.breezyweather.unit.temperature.Temperature.Companion.celsius
import retrofit2.Retrofit
import java.util.Date
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.seconds

class GeoSphereAtService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource {

    override val id = "geosphereat"
    private val countryName = context.currentLocale.getCountryName("AT")
    override val name = "GeoSphere Austria".let {
        if (it.contains(countryName)) {
            it
        } else {
            "$it ($countryName)"
        }
    }
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl = "https://www.geosphere.at/de/legal"

    private val mApi by lazy {
        client
            .baseUrl(GEOSPHERE_AT_BASE_URL)
            .build()
            .create(GeoSphereAtApi::class.java)
    }
    private val mWarningApi by lazy {
        client
            .baseUrl(GEOSPHERE_AT_WARNINGS_BASE_URL)
            .build()
            .create(GeoSphereAtWarningApi::class.java)
    }

    private val weatherAttribution = "GeoSphere Austria (Creative Commons Attribution 4.0)"
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.AIR_QUALITY to weatherAttribution,
        SourceFeature.MINUTELY to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution
    )
    override val attributionLinks = mapOf(
        "GeoSphere Austria" to "https://www.geosphere.at/"
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        val latLng = LatLng(location.latitude, location.longitude)
        return when (feature) {
            SourceFeature.FORECAST -> hourlyBbox.contains(latLng)
            SourceFeature.AIR_QUALITY -> airQuality9KmBbox.contains(latLng)
            SourceFeature.MINUTELY -> nowcastBbox.contains(latLng)
            SourceFeature.ALERT -> location.countryCode.equals("AT", ignoreCase = true)
            else -> false
        }
    }

    /**
     * We don’t recommend forecast as it’s way too light, compared to other sources
     * Highest priority for Austria
     * High priority for neighbour countries
     */
    override fun getFeaturePriorityForLocation(
        location: Location,
        feature: SourceFeature,
    ): Int {
        return when {
            location.countryCode.equals("AT", ignoreCase = true) &&
                feature != SourceFeature.FORECAST -> PRIORITY_HIGHEST
            feature == SourceFeature.MINUTELY &&
                isFeatureSupportedForLocation(location, feature) -> PRIORITY_HIGH
            else -> PRIORITY_NONE
        }
    }

    private val airQualityParameters = arrayOf(
        "pm25surf",
        "pm10surf",
        "no2surf",
        "o3surf"
    )

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()
        val hourly = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getHourlyForecast(
                "${location.latitude},${location.longitude}",
                arrayOf(
                    "sy", // Weather symbol
                    "t2m", // Temperature at 2 meters
                    "rr_acc", // Total precipitation amount
                    "rain_acc", // Total rainfall amount
                    "snow_acc", // Total surface snow amount
                    "u10m", // 10 m wind speed in eastward direction
                    "ugust", // u component of maximum wind gust
                    "v10m", // 10 m wind speed in northward direction
                    "vgust", // v component of maximum wind gust
                    "rh2m", // Relative humidity 2 meters
                    "tcc", // Total cloud cover
                    "sp" // Surface pressure
                ).joinToString(",")
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(GeoSphereAtTimeseriesResult())
            }
        } else {
            Observable.just(GeoSphereAtTimeseriesResult())
        }

        val airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
            val latLng = LatLng(location.latitude, location.longitude)
            mApi.getAirQuality(
                if (airQuality3KmBbox.contains(latLng)) 3 else 9,
                "${location.latitude},${location.longitude}",
                airQualityParameters.joinToString(",")
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.AIR_QUALITY] = it
                Observable.just(GeoSphereAtTimeseriesResult())
            }
        } else {
            Observable.just(GeoSphereAtTimeseriesResult())
        }

        val nowcast = if (SourceFeature.MINUTELY in requestedFeatures) {
            mApi.getNowcast(
                "${location.latitude},${location.longitude}",
                "rr" // precipitation sum
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.MINUTELY] = it
                Observable.just(GeoSphereAtTimeseriesResult())
            }
        } else {
            Observable.just(GeoSphereAtTimeseriesResult())
        }

        val alerts = if (SourceFeature.ALERT in requestedFeatures) {
            mWarningApi.getWarningsForCoords(
                location.longitude,
                location.latitude,
                if (context.currentLocale.code == "de") "de" else "en"
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.ALERT] = it
                Observable.just(GeoSphereAtWarningsResult())
            }
        } else {
            Observable.just(GeoSphereAtWarningsResult())
        }

        return Observable.zip(
            hourly,
            airQuality,
            nowcast,
            alerts
        ) { hourlyResult, airQualityResult, nowcastResult, alertsResult ->
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(hourlyResult, location)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(hourlyResult)
                } else {
                    null
                },
                airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
                    val airQualityHourly = mutableMapOf<Date, AirQuality>()

                    if (!airQualityResult.timestamps.isNullOrEmpty() &&
                        airQualityResult.features?.getOrNull(0)?.properties?.parameters != null
                    ) {
                        airQualityResult.timestamps.forEachIndexed { i, date ->
                            airQualityHourly[date] = airQualityResult.features[0].properties!!.parameters!!.let {
                                AirQuality(
                                    pM25 = it.pm25surf?.data?.getOrNull(i)?.microgramsPerCubicMeter,
                                    pM10 = it.pm10surf?.data?.getOrNull(i)?.microgramsPerCubicMeter,
                                    nO2 = it.no2surf?.data?.getOrNull(i)?.microgramsPerCubicMeter,
                                    o3 = it.o3surf?.data?.getOrNull(i)?.microgramsPerCubicMeter
                                )
                            }
                        }
                    }
                    AirQualityWrapper(hourlyForecast = airQualityHourly)
                } else {
                    null
                },
                minutelyForecast = if (SourceFeature.MINUTELY in requestedFeatures) {
                    getMinutelyForecast(nowcastResult)
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlerts(alertsResult)
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    /**
     * Returns daily forecast from hourly forecast
     */
    private fun getDailyForecast(
        hourlyResult: GeoSphereAtTimeseriesResult,
        location: Location,
    ): List<DailyWrapper> {
        if (hourlyResult.timestamps.isNullOrEmpty()) return emptyList()
        val dayList = hourlyResult.timestamps.map {
            it.getIsoFormattedDate(location)
        }.distinct()

        val dailyList = mutableListOf<DailyWrapper>()
        for (i in 0 until dayList.size - 1) {
            val dayDate = dayList[i].toDateNoHour(location.timeZone)
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
        hourlyResult: GeoSphereAtTimeseriesResult,
    ): List<HourlyWrapper> {
        if (hourlyResult.timestamps.isNullOrEmpty() ||
            hourlyResult.features?.getOrNull(0)?.properties?.parameters == null
        ) {
            return emptyList()
        }
        return hourlyResult.timestamps.mapIndexed { i, date ->
            // Wind
            val windU = hourlyResult.features[0].properties!!.parameters!!.u10m?.data?.getOrNull(i)
            val windV = hourlyResult.features[0].properties!!.parameters!!.v10m?.data?.getOrNull(i)
            val windGustU = hourlyResult.features[0].properties!!.parameters!!.ugust?.data?.getOrNull(i)
            val windGustV = hourlyResult.features[0].properties!!.parameters!!.vgust?.data?.getOrNull(i)
            val windDegree = if (windU != null && windV != null) {
                if (windU == 0.0) {
                    -1.0
                } else {
                    // I have absolutely no idea what I'm doing
                    // https://confluence.ecmwf.int/pages/viewpage.action?pageId=133262398
                    (180 + (180 / Math.PI) * atan2(windU, windV)).mod(360.0)
                }
            } else {
                null
            }
            val windSpeed = if (windU != null && windV != null) {
                sqrt(windU.pow(2) + windV.pow(2))
            } else {
                null
            }
            val windGustSpeed = if (windGustU != null && windGustV != null) {
                sqrt(windGustU.pow(2) + windGustV.pow(2))
            } else {
                null
            }

            HourlyWrapper(
                date = date,
                weatherCode = getWeatherCode(hourlyResult.features[0].properties!!.parameters!!.sy?.data?.getOrNull(i)),
                temperature = TemperatureWrapper(
                    temperature = hourlyResult.features[0].properties!!.parameters!!.t2m?.data?.getOrNull(i)?.celsius
                ),
                precipitation = Precipitation(
                    total = hourlyResult.features[0].properties!!.parameters!!.rrAcc?.data?.getOrNull(i)
                        .minus(hourlyResult.features[0].properties!!.parameters!!.rrAcc?.data?.getOrNull(i - 1))
                        ?.millimeters,
                    rain = hourlyResult.features[0].properties!!.parameters!!.rainAcc?.data?.getOrNull(i)
                        .minus(hourlyResult.features[0].properties!!.parameters!!.rainAcc?.data?.getOrNull(i - 1))
                        ?.millimeters,
                    snow = hourlyResult.features[0].properties!!.parameters!!.snowAcc?.data?.getOrNull(i)
                        .minus(hourlyResult.features[0].properties!!.parameters!!.snowAcc?.data?.getOrNull(i - 1))
                        ?.millimeters
                ),
                wind = if (windSpeed != null) {
                    Wind(
                        degree = windDegree,
                        speed = windSpeed.metersPerSecond,
                        gusts = windGustSpeed?.metersPerSecond
                    )
                } else {
                    null
                },
                relativeHumidity = hourlyResult.features[0].properties!!.parameters!!.rh2m?.data?.getOrNull(i)?.percent,
                pressure = hourlyResult.features[0].properties!!.parameters!!.sp?.data?.getOrNull(i)?.pascals,
                cloudCover = hourlyResult.features[0].properties!!.parameters!!.tcc?.data?.getOrNull(i)?.fraction
            )
        }
    }

    private fun getMinutelyForecast(
        nowcastResult: GeoSphereAtTimeseriesResult,
    ): List<Minutely>? {
        if (nowcastResult.timestamps.isNullOrEmpty() ||
            nowcastResult.features?.getOrNull(0)?.properties?.parameters?.rr?.data == null
        ) {
            return null
        }

        return nowcastResult.timestamps.mapIndexed { i, date ->
            Minutely(
                date = date,
                minuteInterval = 15,
                /**
                 * If I understand correctly, the unit is kg/m², which is approximately the same as mm
                 * However, since it's 15 min by 15 min, and we want mm/h unit, we just have to multiply
                 * by 4, right?
                 */
                precipitationIntensity = nowcastResult.features[0].properties!!.parameters!!.rr!!.data!!.getOrNull(i)
                    ?.times(4)
                    ?.millimeters
            )
        }
    }

    private fun getAlerts(alertsResult: GeoSphereAtWarningsResult): List<Alert>? {
        if (alertsResult.properties?.warnings == null) return null
        return alertsResult.properties.warnings.map { result ->
            Alert(
                alertId = result.properties.warnid.toString(),
                startDate = result.properties.rawinfo?.start?.toLongOrNull()?.seconds?.inWholeMilliseconds?.toDate(),
                endDate = result.properties.rawinfo?.end?.toLongOrNull()?.seconds?.inWholeMilliseconds?.toDate(),
                headline = result.properties.text,
                description = if (!result.properties.meteotext.isNullOrEmpty() ||
                    !result.properties.consequences.isNullOrEmpty()
                ) {
                    "${result.properties.meteotext.let {
                        if (!it.isNullOrEmpty()) it + "\n\n" else ""
                    }}${result.properties.consequences ?: ""}"
                } else {
                    null
                },
                instruction = result.properties.instructions,
                source = "GeoSphere Austria",
                severity = when (result.properties.rawinfo?.wlevel) {
                    3 -> AlertSeverity.EXTREME
                    2 -> AlertSeverity.SEVERE
                    1 -> AlertSeverity.MODERATE
                    else -> AlertSeverity.UNKNOWN
                },
                color = when (result.properties.rawinfo?.wlevel) {
                    3 -> Color.rgb(255, 0, 0)
                    2 -> Color.rgb(255, 196, 0)
                    1 -> Color.rgb(255, 255, 0)
                    else -> Alert.colorFromSeverity(AlertSeverity.UNKNOWN)
                }
            )
        }
    }

    /**
     * From https://github.com/breezy-weather/breezy-weather/issues/763#issuecomment-2044440950
     */
    private fun getWeatherCode(icon: Double?): WeatherCode? {
        if (icon == null) return null
        return when (icon.roundToInt()) {
            1, 2 -> WeatherCode.CLEAR
            3 -> WeatherCode.PARTLY_CLOUDY
            4, 5 -> WeatherCode.CLOUDY
            6, 7 -> WeatherCode.FOG
            8, 9, 10, 17, 18, 19 -> WeatherCode.RAIN
            11, 12, 13, 20, 21, 22 -> WeatherCode.SLEET
            14, 15, 16, 23, 24, 25 -> WeatherCode.SNOW
            26, 27, 28, 29, 30, 31, 32 -> WeatherCode.THUNDERSTORM
            else -> null
        }
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val GEOSPHERE_AT_BASE_URL = "https://dataset.api.hub.geosphere.at/"
        private const val GEOSPHERE_AT_WARNINGS_BASE_URL = "https://warnungen.zamg.at/wsapp/api/"

        val hourlyBbox = LatLngBounds.parse(west = 5.49, south = 42.98, east = 22.1, north = 51.82)
        val airQuality3KmBbox = LatLngBounds.parse(west = 2.86, south = 40.91, east = 23.74, north = 53.75)
        val airQuality9KmBbox = LatLngBounds.parse(west = -53.73, south = 15.59, east = 80.33, north = 74.39)
        val nowcastBbox = LatLngBounds.parse(west = 8.1, south = 45.5, east = 17.74, north = 49.48)
    }
}
