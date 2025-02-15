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
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.wrappers.AirQualityWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
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
    override val name = "LVĢMC (${Locale(context.currentLocale.code, "LV").displayCountry})"
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl = ""

    private val mApi by lazy {
        client.baseUrl(LVGMC_BASE_URL)
            .build()
            .create(LvgmcApi::class.java)
    }

    private val weatherAttribution = "Latvijas Vides, ģeoloģijas un meteoroloģijas centrs"
    override val reverseGeocodingAttribution = weatherAttribution
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.AIR_QUALITY to weatherAttribution
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return isReverseGeocodingSupportedForLocation(location)
    }

    override fun isReverseGeocodingSupportedForLocation(location: Location): Boolean {
        return location.countryCode.equals("LV", ignoreCase = true)
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
    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location,
    ): Observable<List<Location>> {
        val formatter = SimpleDateFormat("yyyyMMddHH", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getTimeZone("Europe/Riga")
        val laiks = formatter.format(Date()) + "00"
        val bounds = getBoundingBox(location)
        return mApi.getForecastLocations(
            laiks = laiks,
            bounds = bounds
        ).map {
            convert(location, it)
        }
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
        val bounds = getBoundingBox(location)
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
        location: Location,
    ): String {
        return "POLYGON((" +
            (location.longitude - 0.1) + " " + (location.latitude - 0.1) + ", " +
            (location.longitude + 0.1) + " " + (location.latitude - 0.1) + ", " +
            (location.longitude + 0.1) + " " + (location.latitude + 0.1) + ", " +
            (location.longitude - 0.1) + " " + (location.latitude + 0.1) + ", " +
            (location.longitude - 0.1) + " " + (location.latitude - 0.1) + "))"
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val LVGMC_BASE_URL = "https://videscentrs.lvgmc.lv/"
    }
}
