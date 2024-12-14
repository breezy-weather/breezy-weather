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
import breezyweather.domain.weather.wrappers.AirQualityWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import com.google.maps.android.model.LatLng
import com.google.maps.android.model.LatLngBounds
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.sources.geosphereat.json.GeoSphereAtTimeseriesResult
import org.breezyweather.sources.geosphereat.json.GeoSphereAtWarningsResult
import retrofit2.Retrofit
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

class GeoSphereAtService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource {

    override val id = "geosphereat"
    val countryName = Locale(context.currentLocale.code, "AT").displayCountry
    override val name = "GeoSphere Austria".let {
        if (it.contains(countryName)) {
            it
        } else {
            "$it ($countryName)"
        }
    }
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl = "https://www.geosphere.at/de/legal"

    override val color = Color.rgb(191, 206, 64)

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

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        val latLng = LatLng(location.latitude, location.longitude)
        return when (feature) {
            SourceFeature.FORECAST -> hourlyBbox.contains(latLng)
            SourceFeature.AIR_QUALITY -> airQuality12KmBbox.contains(latLng)
            SourceFeature.MINUTELY -> nowcastBbox.contains(latLng)
            SourceFeature.ALERT -> location.countryCode.equals("AT", ignoreCase = true)
            else -> false
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
        val failedFeatures = mutableListOf<SourceFeature>()
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
                failedFeatures.add(SourceFeature.FORECAST)
                Observable.just(GeoSphereAtTimeseriesResult())
            }
        } else {
            Observable.just(GeoSphereAtTimeseriesResult())
        }

        val airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
            val latLng = LatLng(location.latitude, location.longitude)
            mApi.getAirQuality(
                if (airQuality4KmBbox.contains(latLng)) 4 else 12,
                "${location.latitude},${location.longitude}",
                airQualityParameters.joinToString(",")
            ).onErrorResumeNext {
                failedFeatures.add(SourceFeature.AIR_QUALITY)
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
                failedFeatures.add(SourceFeature.MINUTELY)
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
                failedFeatures.add(SourceFeature.ALERT)
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
                    getHourlyForecast(hourlyResult, airQualityResult)
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
                                    pM25 = it.pm25surf?.data?.getOrNull(i),
                                    pM10 = it.pm10surf?.data?.getOrNull(i),
                                    nO2 = it.no2surf?.data?.getOrNull(i),
                                    o3 = it.o3surf?.data?.getOrNull(i)
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

    companion object {
        private const val GEOSPHERE_AT_BASE_URL = "https://dataset.api.hub.geosphere.at/"
        private const val GEOSPHERE_AT_WARNINGS_BASE_URL = "https://warnungen.zamg.at/wsapp/api/"

        val hourlyBbox = LatLngBounds.parse(west = 5.49, south = 42.98, east = 22.1, north = 51.82)
        val airQuality12KmBbox = LatLngBounds.parse(west = -59.21, south = 17.65, east = 83.21, north = 76.49)
        val airQuality4KmBbox = LatLngBounds.parse(west = 4.31, south = 41.72, east = 18.99, north = 50.15)
        val nowcastBbox = LatLngBounds.parse(west = 8.1, south = 45.5, east = 17.74, north = 49.48)
    }
}
