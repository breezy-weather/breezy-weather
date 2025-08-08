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

package org.breezyweather.sources.lvgmc

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.location.model.LocationAddressInfo
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.wrappers.AirQualityWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import com.google.maps.android.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.sources.lvgmc.json.LvgmcAirQualityLocationResult
import org.breezyweather.sources.lvgmc.json.LvgmcAirQualityResult
import org.breezyweather.sources.lvgmc.json.LvgmcCurrentLocation
import org.breezyweather.sources.lvgmc.json.LvgmcCurrentResult
import org.breezyweather.sources.lvgmc.json.LvgmcForecastResult
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Named
import kotlin.time.Duration.Companion.hours

class LvgmcService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ReverseGeocodingSource, LocationParametersSource {
    override val id = "lvgmc"
    override val name = "LVĢMC (${context.currentLocale.getCountryName("LV")})"
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl = ""

    private val mApi by lazy {
        client.baseUrl(LVGMC_BASE_URL)
            .build()
            .create(LvgmcApi::class.java)
    }

    private val weatherAttribution = "Latvijas Vides, ģeoloģijas un meteoroloģijas centrs"
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.AIR_QUALITY to weatherAttribution,
        SourceFeature.REVERSE_GEOCODING to weatherAttribution
    )
    override val attributionLinks = mapOf(
        weatherAttribution to LVGMC_BASE_URL
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return location.countryCode.equals("LV", ignoreCase = true)
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
        val currentLocation = location.parameters.getOrElse(id) { null }?.getOrElse("currentLocation") { null }
        val forecastLocation = location.parameters.getOrElse(id) { null }?.getOrElse("forecastLocation") { null }
        val airQualityLocation = location.parameters.getOrElse(id) { null }?.getOrElse("airQualityLocation") { null }

        if (currentLocation.isNullOrEmpty() || forecastLocation.isNullOrEmpty() || airQualityLocation.isNullOrEmpty()) {
            return Observable.error(InvalidLocationException())
        }

        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()

        val daily = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getForecast(
                scope = "daily",
                punkts = forecastLocation
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }
        val hourly = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getForecast(
                scope = "hourly",
                punkts = forecastLocation
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }

        val current = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getCurrent().onErrorResumeNext {
                failedFeatures[SourceFeature.CURRENT] = it
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }

        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getTimeZone("Europe/Riga")
        val fromDate = formatter.format(Date(Date().time - 24.hours.inWholeMilliseconds))
        val airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
            mApi.getAirQuality(
                station = airQualityLocation,
                fromDate = fromDate
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.AIR_QUALITY] = it
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }

        return Observable.zip(current, daily, hourly, airQuality) {
                currentResult: List<LvgmcCurrentResult>,
                dailyResult: List<LvgmcForecastResult>,
                hourlyResult: List<LvgmcForecastResult>,
                aq: List<LvgmcAirQualityResult>,
            ->
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(context, dailyResult)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(context, hourlyResult)
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(location, currentResult)
                } else {
                    null
                },
                airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
                    AirQualityWrapper(
                        current = AirQuality(
                            pM25 = aq.filter { it.code == "PM2.5_60min" }.sortedByDescending { it.time }
                                .firstOrNull()?.value,
                            pM10 = aq.filter { it.code == "PM10_60min" }.sortedByDescending { it.time }
                                .firstOrNull()?.value,
                            sO2 = aq.filter { it.code == "SO2" }.sortedByDescending { it.time }.firstOrNull()?.value,
                            nO2 = aq.filter { it.code == "NO2" }.sortedByDescending { it.time }.firstOrNull()?.value,
                            o3 = aq.filter { it.code == "O3" }.sortedByDescending { it.time }.firstOrNull()?.value,
                            cO = aq.filter { it.code == "CO" }.sortedByDescending { it.time }.firstOrNull()?.value
                        )
                    )
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    // REVERSE GEOCODING
    override fun requestNearestLocation(
        context: Context,
        latitude: Double,
        longitude: Double,
    ): Observable<List<LocationAddressInfo>> {
        val formatter = SimpleDateFormat("yyyyMMddHH", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getTimeZone("Europe/Riga")
        val laiks = formatter.format(Date()) + "00"
        val bounds = getBoundingBox(latitude, longitude)
        return mApi.getForecastLocations(
            laiks = laiks,
            bounds = bounds
        ).map {
            convertLocation(latitude, longitude, it)
        }
    }

    private fun convertLocation(
        latitude: Double,
        longitude: Double,
        forecastLocationsResult: List<LvgmcForecastResult>,
    ): List<LocationAddressInfo> {
        val locationList = mutableListOf<LocationAddressInfo>()
        val forecastLocations = forecastLocationsResult.filter {
            it.point != null && it.latitude != null && it.longitude != null
        }.associate {
            it.point!! to LatLng(it.latitude!!.toDouble(), it.longitude!!.toDouble())
        }
        val forecastLocation = LatLng(latitude, longitude).getNearestLocation(forecastLocations)

        forecastLocationsResult.firstOrNull { it.point == forecastLocation }?.let {
            locationList.add(
                LocationAddressInfo(
                    timeZoneId = "Europe/Riga",
                    countryCode = "LV",
                    admin1 = it.municipality,
                    city = it.name
                )
            )
        }
        return locationList
    }

    // LOCATION PARAMETERS
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        if (coordinatesChanged) return true

        val currentLocation = location.parameters.getOrElse(id) { null }?.getOrElse("currentLocation") { null }
        val forecastLocation = location.parameters.getOrElse(id) { null }?.getOrElse("forecastLocation") { null }
        val airQualityLocation = location.parameters.getOrElse(id) { null }?.getOrElse("airQualityLocation") { null }

        return currentLocation.isNullOrEmpty() || forecastLocation.isNullOrEmpty() || airQualityLocation.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        val formatter = SimpleDateFormat("yyyyMMddHH", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getTimeZone("Europe/Riga")
        val laiks = formatter.format(Date()) + "00"
        val bounds = getBoundingBox(location.latitude, location.longitude)
        val forecastLocations = mApi.getForecastLocations(
            laiks = laiks,
            bounds = bounds
        )
        val currentLocations = mApi.getCurrentLocations()
        val airQualityLocations = mApi.getAirQualityLocations()

        return Observable.zip(currentLocations, forecastLocations, airQualityLocations) {
                currentLocationsResult: List<LvgmcCurrentLocation>,
                forecastLocationsResult: List<LvgmcForecastResult>,
                airQualityLocationsResult: List<LvgmcAirQualityLocationResult>,
            ->
            convert(location, currentLocationsResult, forecastLocationsResult, airQualityLocationsResult)
        }
    }

    private fun getBoundingBox(
        latitude: Double,
        longitude: Double,
    ): String {
        return "POLYGON((" +
            "${longitude - 0.1} ${latitude - 0.1}, " +
            "${longitude + 0.1} ${latitude - 0.1}, " +
            "${longitude + 0.1} ${latitude + 0.1}, " +
            "${longitude - 0.1} ${latitude + 0.1}, " +
            "${longitude - 0.1} ${latitude - 0.1}))"
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val LVGMC_BASE_URL = "https://videscentrs.lvgmc.lv/"
    }
}
