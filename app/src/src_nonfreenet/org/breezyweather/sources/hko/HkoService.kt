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

package org.breezyweather.sources.hko

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.location.model.LocationAddressInfo
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.wrappers.WeatherWrapper
import com.google.maps.android.PolyUtil
import com.google.maps.android.data.geojson.GeoJsonMultiPolygon
import com.google.maps.android.data.geojson.GeoJsonParser
import com.google.maps.android.data.geojson.GeoJsonPolygon
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.sources.hko.json.HkoCurrentResult
import org.breezyweather.sources.hko.json.HkoForecastResult
import org.breezyweather.sources.hko.json.HkoNormalsResult
import org.breezyweather.sources.hko.json.HkoOneJsonResult
import org.breezyweather.sources.hko.json.HkoWarningResult
import org.json.JSONObject
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.round

class HkoService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ReverseGeocodingSource, LocationParametersSource {

    override val id = "hko"
    override val name by lazy {
        if (context.currentLocale.code.startsWith("zh")) {
            "香港天文台"
        } else {
            "HKO (${context.currentLocale.getCountryName("HK")})"
        }
    }
    override val continent = SourceContinent.ASIA
    override val privacyPolicyUrl by lazy {
        with(context.currentLocale.code) {
            when {
                equals("zh-tw") || equals("zh-hk") || equals("zh-mo") -> "https://www.hko.gov.hk/tc/privacy/policy.htm"
                startsWith("zh") -> "https://www.hko.gov.hk/sc/privacy/policy.htm"
                else -> "https://www.hko.gov.hk/en/privacy/policy.htm"
            }
        }
    }

    private val mApi by lazy {
        client
            .baseUrl(HKO_BASE_URL)
            .build()
            .create(HkoApi::class.java)
    }

    private val mMapsApi by lazy {
        client
            .baseUrl(HKO_MAPS_BASE_URL)
            .build()
            .create(HkoMapsApi::class.java)
    }

    private val weatherAttribution by lazy {
        if (context.currentLocale.code.startsWith("zh")) {
            "香港天文台"
        } else {
            "Hong Kong Observatory"
        }
    }
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution,
        SourceFeature.NORMALS to weatherAttribution,
        SourceFeature.REVERSE_GEOCODING to weatherAttribution
    )
    override val attributionLinks = mapOf(
        weatherAttribution to "https://www.hko.gov.hk/"
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return location.countryCode.equals("HK", ignoreCase = true)
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
        val languageCode = context.currentLocale.code

        // Make sure we have current grid ID, forecast grid ID, and default weather station.
        // We need these to call the APIs.
        val currentGrid = location.parameters.getOrElse(id) { null }?.getOrElse("currentGrid") { null }
        val currentStation = location.parameters.getOrElse(id) { null }?.getOrElse("currentStation") { null }
        val forecastGrid = location.parameters.getOrElse(id) { null }?.getOrElse("forecastGrid") { null }
        if (currentGrid.isNullOrEmpty() || currentStation.isNullOrEmpty() || forecastGrid.isNullOrEmpty()) {
            return Observable.error(InvalidLocationException())
        }

        // Several of the API endpoints output text in English and Traditional Chinese,
        // while text in Simplified Chinese are at an endpoint under different path.
        val path = if (languageCode.startsWith("zh") &&
            languageCode != "zh-tw" &&
            languageCode != "zh-hk" &&
            languageCode != "zh-mo"
        ) {
            HKO_SIMPLIFIED_CHINESE_PATH
        } else {
            ""
        }

        val warnings = mutableMapOf<String, HkoWarningResult>()
        var warningKey: String
        var endPoint: String

        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()
        val forecast = if (SourceFeature.FORECAST in requestedFeatures) {
            mMapsApi.getForecast(
                grid = forecastGrid,
                v = System.currentTimeMillis()
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(HkoForecastResult())
            }
        } else {
            Observable.just(HkoForecastResult())
        }

        // CURRENT
        // Full current observation takes two API calls: getCurrentWeather and getOneJson
        val current = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getCurrentWeather(
                grid = currentGrid
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.CURRENT] = it
                Observable.just(HkoCurrentResult())
            }
        } else {
            Observable.just(HkoCurrentResult())
        }

        val oneJson = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getOneJson(
                path = path,
                suffix = if (languageCode.startsWith("zh")) {
                    "_uc"
                } else {
                    ""
                }
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.CURRENT] = it
                Observable.just(HkoOneJsonResult())
            }
        } else {
            Observable.just(HkoOneJsonResult())
        }

        // Keys in Warning Summary endpoint end in different suffixes depending on the language
        val suffix = if (languageCode.startsWith("zh")) "_C" else "_E"

        // ALERTS
        // First read the warning summary file.
        // Loop through each warning type in the summary; check which ones are current.
        // Then only load the detailed files of the current warning types.
        val warningDetails = if (SourceFeature.ALERT in requestedFeatures) {
            mApi.getWarningSummary(
                path = path
            ).map { warningSummary ->
                warningSummary.DYN_DAT_WARNSUM?.forEach {
                    if (it.key.endsWith(suffix)) {
                        warningKey = it.key.substring(0, it.key.length - 2)
                        if (HKO_WARNING_ENDPOINTS.containsKey(warningKey)) {
                            endPoint = HKO_WARNING_ENDPOINTS[warningKey]!!
                            if (it.value.Warning_Action != null &&
                                it.value.Warning_Action != "" &&
                                it.value.Warning_Action != "CANCEL"
                            ) {
                                warnings[endPoint] = mApi.getWarningText(path, endPoint).onErrorResumeNext {
                                    failedFeatures[SourceFeature.ALERT] = it
                                    Observable.just(HkoWarningResult())
                                }.blockingFirst()
                            }
                        }
                    }
                }
                warnings
            }.onErrorResumeNext {
                failedFeatures[SourceFeature.ALERT] = it
                Observable.just(mutableMapOf())
            }
        } else {
            Observable.just(mutableMapOf())
        }

        // NORMALS
        // HKO has its own normals endpoint from all the other stations.
        val normals = if (SourceFeature.NORMALS in requestedFeatures) {
            when (currentStation) {
                "HKO" -> mApi.getHkoNormals().onErrorResumeNext {
                    failedFeatures[SourceFeature.NORMALS] = it
                    Observable.just(HkoNormalsResult())
                }
                else -> mApi.getNormals(currentStation).onErrorResumeNext {
                    failedFeatures[SourceFeature.NORMALS] = it
                    Observable.just(HkoNormalsResult())
                }
            }
        } else {
            Observable.just(HkoNormalsResult())
        }

        return Observable.zip(current, forecast, normals, oneJson, warningDetails) {
                currentResult: HkoCurrentResult,
                forecastResult: HkoForecastResult,
                normalsResult: HkoNormalsResult,
                oneJsonResult: HkoOneJsonResult,
                warningDetailsResult: MutableMap<String, HkoWarningResult>,
            ->
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(
                        context,
                        forecastResult.DailyForecast,
                        oneJsonResult
                    )
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(context, forecastResult.HourlyWeatherForecast, oneJsonResult)
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(context, currentResult.RegionalWeather, oneJsonResult)
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlertList(context, warningDetailsResult)
                } else {
                    null
                },
                normals = if (SourceFeature.NORMALS in requestedFeatures) {
                    getNormals(normalsResult)
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    // Reverse geocoding
    override fun requestNearestLocation(
        context: Context,
        location: Location,
    ): Observable<List<LocationAddressInfo>> {
        val languageCode = context.currentLocale.code
        val currentGrid = getCurrentGrid(location)
        return mApi.getLocations(currentGrid).map { locationResult ->
            val json = "{\"type\":\"FeatureCollection\",\"features\":${locationResult.features}}"
            val geoJsonParser = GeoJsonParser(JSONObject(json))
            val matchingLocations = geoJsonParser.features.filter { feature ->
                when (feature.geometry) {
                    is GeoJsonPolygon -> (feature.geometry as GeoJsonPolygon).coordinates.any { polygon ->
                        PolyUtil.containsLocation(location.latitude, location.longitude, polygon, true)
                    }
                    is GeoJsonMultiPolygon -> (feature.geometry as GeoJsonMultiPolygon).polygons.any {
                        it.coordinates.any { polygon ->
                            PolyUtil.containsLocation(location.latitude, location.longitude, polygon, true)
                        }
                    }
                    else -> false
                }
            }
            matchingLocations.map {
                LocationAddressInfo(
                    timeZoneId = "Asia/Hong_Kong",
                    countryCode = "HK",
                    city = with(languageCode) {
                        when {
                            equals("zh-tw") || equals("zh-hk") || equals("zh-mo") -> {
                                it.getProperty("tc") ?: it.getProperty("sc") ?: it.getProperty("en")
                            }
                            startsWith("zh") -> {
                                it.getProperty("sc") ?: it.getProperty("tc") ?: it.getProperty("en")
                            }
                            else -> {
                                it.getProperty("en") ?: it.getProperty("sc") ?: it.getProperty("tc")
                            }
                        }
                    }
                )
            }
        }
    }

    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        if (coordinatesChanged) return true

        // IDs for current observation grid and forecast grid are different.
        // Also need to make sure we have a valid station for normals.
        val currentGrid = location.parameters.getOrElse(id) { null }?.getOrElse("currentGrid") { null }
        val currentStation = location.parameters.getOrElse(id) { null }?.getOrElse("currentStation") { null }
        val forecastGrid = location.parameters.getOrElse(id) { null }?.getOrElse("forecastGrid") { null }

        return currentGrid.isNullOrEmpty() || currentStation.isNullOrEmpty() || forecastGrid.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        // Forecast grid is valid between (21.75°N–23.25°N, 113.35°E–114.95°E).
        // Current grid is valid between (22.13°N–22.57°N, 113.82°E–114.54°E).
        // If the location is within the boundaries of the current grid,
        // it is definitely within the boundaries of the forecast grid.
        if (location.latitude < HKO_CURRENT_GRID_LATITUDES.first() ||
            location.latitude >= HKO_CURRENT_GRID_LATITUDES.last() ||
            location.longitude < HKO_CURRENT_GRID_LONGITUDES.first() ||
            location.longitude >= HKO_CURRENT_GRID_LONGITUDES.last()
        ) {
            throw InvalidLocationException()
        }
        val currentGrid = getCurrentGrid(location)

        // Obtain the default weather station for temperature normals
        val station = mApi.getCurrentWeather(currentGrid).onErrorResumeNext {
            // TODO: Log warning
            Observable.just(HkoCurrentResult())
        }.blockingFirst().RegionalWeather?.Temp?.DefaultStation
        if (station == null) {
            throw InvalidLocationException()
        }

        // Identify grid ID for Forecast.
        // It is in a 16 E-W × 15-N-S configuration, numbered from 1 to 240.
        // Each block is 0.1° latitude and 0.1° longitude in size.
        val row = (232 - round(location.latitude * 10)).toInt()
        val column = (round(location.longitude * 10) - 1133).toInt()
        val forecastGrid = "G" + (row * 16 + column).toString()

        return Observable.just(
            mapOf(
                "currentGrid" to currentGrid,
                "currentStation" to station,
                "forecastGrid" to forecastGrid
            )
        )
    }

    private fun getCurrentGrid(
        location: Location,
    ): String {
        // Make sure the location is within the boundaries of the current grid.
        if (location.latitude < HKO_CURRENT_GRID_LATITUDES.first() ||
            location.latitude >= HKO_CURRENT_GRID_LATITUDES.last() ||
            location.longitude < HKO_CURRENT_GRID_LONGITUDES.first() ||
            location.longitude >= HKO_CURRENT_GRID_LONGITUDES.last()
        ) {
            throw InvalidLocationException()
        }
        var row: Int? = null
        var column: Int? = null

        // Identify grid ID for Current Observation and Reverse Geolocation.
        // Current observations are in a 18 E-W × 18-N-S configuration, numbered from 0101 to 1818.
        // The grid spacing is uneven, hence we iterate through the lists of the grid boundaries.
        var i = 1
        while (i < HKO_CURRENT_GRID_LATITUDES.size && row == null) {
            if (location.latitude >= HKO_CURRENT_GRID_LATITUDES[i - 1] &&
                location.latitude < HKO_CURRENT_GRID_LATITUDES[i]
            ) {
                row = i
            }
            i++
        }

        i = 1
        while (i < HKO_CURRENT_GRID_LONGITUDES.size && column == null) {
            if (location.longitude >= HKO_CURRENT_GRID_LONGITUDES[i - 1] &&
                location.longitude < HKO_CURRENT_GRID_LONGITUDES[i]
            ) {
                column = i
            }
            i++
        }

        // Just double check that we got a valid grid ID
        if (row == null || column == null) {
            throw InvalidLocationException()
        }
        return column.toString().padStart(2, '0') + row.toString().padStart(2, '0')
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val HKO_BASE_URL = "https://www.hko.gov.hk/"
        private const val HKO_MAPS_BASE_URL = "https://maps.weather.gov.hk/"
        private const val HKO_SIMPLIFIED_CHINESE_PATH = "dps/sc/"

        // Some Warning Summary records have different keys from the corresponding end points
        private val HKO_WARNING_ENDPOINTS = mapOf<String, String>(
            "WTCPRE8" to "WTCPRE8", // Pre-8 Tropical Cyclone Special Announcement
            "WTCSGNL" to "WTCB", // Tropical Cyclone Warning Signal
            "WRAIN" to "WRAINSA", // Rainstorm Warning Signal
            "WTMW" to "WTMW", // Tsunami Warning
            "WFNTSA" to "WFNTSA", // Special Announcement on Flooding in Northern New Territories
            "WMSGNL" to "WMNB", // Strong Monsoon Signal
            "WL" to "WLSA", // Landslip Warning
            "WTS" to "WTS", // Thunderstorm Warning
            "WFIRE" to "WFIRE", // Fire Danger Warning
            "WHOT" to "WHOT", // Very Hot Weather Warning
            "WCOLD" to "WCOLD", // Cold Weather Warning
            "WFROST" to "WFROST" // Frost Warning
        )

        // Current grid boundaries are taken from
        // https://my.weather.gov.hk/hiking/geojson/grid.geojson
        // We're not having Breeze Weather download the file itself each time
        // because it's static and unnecessarily big.
        private val HKO_CURRENT_GRID_LONGITUDES = listOf(
            113.816666666666663,
            113.88666666666667,
            113.956666666666663,
            113.996666666666655,
            114.036666666666662,
            114.076666666666654,
            114.11666666666666,
            114.136666666666656,
            114.156666666666666,
            114.176666666666662,
            114.196666666666658,
            114.216666666666669,
            114.236666666666665,
            114.276666666666671,
            114.316666666666663,
            114.356666666666669,
            114.396666666666661,
            114.466666666666654,
            114.536666666666662
        )
        private val HKO_CURRENT_GRID_LATITUDES = listOf(
            22.133333333333333,
            22.173333333333336,
            22.213333333333335,
            22.253333333333334,
            22.273333333333333,
            22.293333333333333,
            22.30833333333333,
            22.323333333333331,
            22.338333333333331,
            22.353333333333332,
            22.368333333333332,
            22.383333333333333,
            22.398333333333333,
            22.41333333333333,
            22.43333333333333,
            22.45333333333333,
            22.493333333333329,
            22.533333333333328,
            22.573333333333331
        )
    }
}
