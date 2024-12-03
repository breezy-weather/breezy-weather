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
import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.SecondaryWeatherSource
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
) : HttpSource(), MainWeatherSource, SecondaryWeatherSource, ReverseGeocodingSource, LocationParametersSource {
    override val id = "lvgmc"
    override val name = "LVĢMC (${Locale(context.currentLocale.code, "LV").displayCountry})"
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl = ""
    override val color = Color.rgb(11, 71, 135)
    override val weatherAttribution = "Latvijas Vides, ģeoloģijas un meteoroloģijas centrs"

    private val mApi by lazy {
        client.baseUrl(LVGMC_BASE_URL)
            .build()
            .create(LvgmcApi::class.java)
    }

    override val supportedFeaturesInMain = listOf(
        SourceFeature.FEATURE_CURRENT,
        SourceFeature.FEATURE_AIR_QUALITY
    )

    override fun isFeatureSupportedInMainForLocation(
        location: Location,
        feature: SourceFeature?,
    ): Boolean {
        return location.countryCode.equals("LV", ignoreCase = true)
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        ignoreFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val currentLocation = location.parameters.getOrElse(id) { null }?.getOrElse("currentLocation") { null }
        val forecastLocation = location.parameters.getOrElse(id) { null }?.getOrElse("forecastLocation") { null }
        val airQualityLocation = location.parameters.getOrElse(id) { null }?.getOrElse("airQualityLocation") { null }

        if (currentLocation.isNullOrEmpty() || forecastLocation.isNullOrEmpty() || airQualityLocation.isNullOrEmpty()) {
            return Observable.error(InvalidLocationException())
        }

        val failedFeatures = mutableListOf<SourceFeature>()

        val daily = mApi.getForecast(
            scope = "daily",
            punkts = forecastLocation
        )
        val hourly = mApi.getForecast(
            scope = "hourly",
            punkts = forecastLocation
        )

        val current = if (!ignoreFeatures.contains(SourceFeature.FEATURE_CURRENT)) {
            mApi.getCurrent().onErrorResumeNext {
                failedFeatures.add(SourceFeature.FEATURE_CURRENT)
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }

        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getTimeZone("Europe/Riga")
        val fromDate = formatter.format(Date(Date().time - 24.hours.inWholeMilliseconds))
        val airQuality = if (!ignoreFeatures.contains(SourceFeature.FEATURE_AIR_QUALITY)) {
            mApi.getAirQuality(
                station = airQualityLocation,
                fromDate = fromDate
            ).onErrorResumeNext {
                failedFeatures.add(SourceFeature.FEATURE_AIR_QUALITY)
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }

        return Observable.zip(current, daily, hourly, airQuality) {
                currentResult: List<LvgmcCurrentResult>,
                dailyResult: List<LvgmcForecastResult>,
                hourlyResult: List<LvgmcForecastResult>,
                airQualityResult: List<LvgmcAirQualityResult>,
            ->
            convert(
                context = context,
                location = location,
                currentResult = currentResult,
                dailyResult = dailyResult,
                hourlyResult = hourlyResult,
                airQualityResult = airQualityResult,
                failedFeatures = failedFeatures
            )
        }
    }

    // SECONDARY WEATHER SOURCE
    override val supportedFeaturesInSecondary = listOf(
        SourceFeature.FEATURE_CURRENT,
        SourceFeature.FEATURE_AIR_QUALITY
    )
    override fun isFeatureSupportedInSecondaryForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return isFeatureSupportedInMainForLocation(location, feature)
    }
    override val currentAttribution = weatherAttribution
    override val airQualityAttribution = weatherAttribution
    override val pollenAttribution = null
    override val minutelyAttribution = null
    override val alertAttribution = null
    override val normalsAttribution = null

    override fun requestSecondaryWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<SecondaryWeatherWrapper> {
        val currentLocation = location.parameters.getOrElse(id) { null }?.getOrElse("currentLocation") { null }
        val airQualityLocation = location.parameters.getOrElse(id) { null }?.getOrElse("airQualityLocation") { null }

        if (currentLocation.isNullOrEmpty() || airQualityLocation.isNullOrEmpty()) {
            return Observable.error(InvalidLocationException())
        }

        val failedFeatures = mutableListOf<SourceFeature>()

        val current = if (requestedFeatures.contains(SourceFeature.FEATURE_CURRENT)) {
            mApi.getCurrent().onErrorResumeNext {
                failedFeatures.add(SourceFeature.FEATURE_CURRENT)
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }

        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getTimeZone("Europe/Riga")
        val fromDate = formatter.format(Date(Date().time - 24.hours.inWholeMilliseconds))
        val airQuality = if (requestedFeatures.contains(SourceFeature.FEATURE_AIR_QUALITY)) {
            mApi.getAirQuality(
                station = airQualityLocation,
                fromDate = fromDate
            ).onErrorResumeNext {
                failedFeatures.add(SourceFeature.FEATURE_AIR_QUALITY)
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }

        return Observable.zip(current, airQuality) {
                currentResult: List<LvgmcCurrentResult>,
                airQualityResult: List<LvgmcAirQualityResult>,
            ->
            convertSecondary(
                location = location,
                currentResult = if (requestedFeatures.contains(SourceFeature.FEATURE_CURRENT)) {
                    currentResult
                } else {
                    null
                },
                airQualityResult = if (requestedFeatures.contains(SourceFeature.FEATURE_AIR_QUALITY)) {
                    airQualityResult
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

    companion object {
        private const val LVGMC_BASE_URL = "https://videscentrs.lvgmc.lv/"
    }
}
