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

package org.breezyweather.sources.ncei

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.Normals
import breezyweather.domain.weather.wrappers.WeatherWrapper
import com.google.maps.android.SphericalUtil
import com.google.maps.android.model.LatLng
import io.reactivex.rxjava3.core.Observable
import kotlinx.serialization.json.Json
import org.breezyweather.common.extensions.roundDownToNearestMultiplier
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.sources.ncei.json.NceiDataResult
import retrofit2.Retrofit
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class NceiService @Inject constructor(
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, LocationParametersSource {

    override val id = "ncei"
    override val name = "NCEI"
    override val continent = SourceContinent.WORLDWIDE
    override val privacyPolicyUrl = "https://www.ncei.noaa.gov/privacy"

    private val mApi by lazy {
        client
            .baseUrl(NCEI_BASE_URL)
            .build()
            .create(NceiApi::class.java)
    }

    private val weatherAttribution = "National Centers for Environmental Information"
    override val supportedFeatures = mapOf(
        SourceFeature.NORMALS to weatherAttribution
    )
    override val attributionLinks = mapOf(
        weatherAttribution to "https://www.ncei.noaa.gov/"
    )

    // NCEI is temporarily disabled for Current Location
    // until better caching is implemented (#1996)
    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return !location.isCurrentPosition
    }

    override fun getFeaturePriorityForLocation(
        location: Location,
        feature: SourceFeature,
    ): Int {
        return when {
            location.countryCode.equals("US", ignoreCase = true) -> PRIORITY_HIGHEST
            else -> PRIORITY_NONE
        }
    }

    override val testingLocations: List<Location> = emptyList()

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val month = Date().toCalendarWithTimeZone(location.javaTimeZone)[Calendar.MONTH] + 1
        val finalYear = (Date().toCalendarWithTimeZone(location.javaTimeZone)[Calendar.YEAR]).toDouble()
            .roundDownToNearestMultiplier(10.0).roundToInt()
        val initialYear = finalYear - 29

        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()

        val stationMap: Map<String, Double> = Json.decodeFromString<Map<String, Double>>(
            location.parameters.getOrElse(id) {
                emptyMap()
            }.getOrElse("stations") { "" }
        )
        val stations = stationMap.keys.joinToString(",")
        val normals = if (stations != "") {
            mApi.getData(
                stations = stations,
                startDate = "$initialYear-01-01",
                endDate = "$finalYear-12-31"
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.NORMALS] = it
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }

        return normals.map {
            WeatherWrapper(
                normals = getNormals(month, it, stationMap),
                failedFeatures = failedFeatures
            )
        }
    }

    private fun getNormals(
        month: Int,
        normalsList: List<NceiDataResult>? = null,
        stationMap: Map<String, Double>,
    ): Normals {
        var tMaxWeightedSum = 0.0
        var tMaxWeightTotal = 0.0
        var tMinWeightedSum = 0.0
        var tMinWeightTotal = 0.0
        val monthEnding: String = if (month in 1..9) {
            "-0$month"
        } else {
            "-$month"
        }

        // Assign a weight to each station as a function of its distance from the weather location.
        // We calculate weights here so that we won't have to force reload location parameters
        // even if the weight function changes in the future.
        val stationWeights = stationMap.mapValues {
            getWeight(it.value)
        }

        // Add each relevant monthly record to the weighted sum of tMax and tMin,
        // using the weight of the reporting station,
        // so that we can calculate the weighted average later.
        normalsList?.forEach {
            if (it.date.endsWith(monthEnding)) {
                if (it.station in stationWeights.keys) {
                    if (it.tMax != null) {
                        tMaxWeightedSum += it.tMax.toDouble().times(stationWeights[it.station]!!)
                        tMaxWeightTotal += stationWeights[it.station]!!
                    }
                    if (it.tMin != null) {
                        tMinWeightedSum += it.tMin.toDouble().times(stationWeights[it.station]!!)
                        tMinWeightTotal += stationWeights[it.station]!!
                    }
                }
            }
        }
        return Normals(
            month = month,
            daytimeTemperature = if (tMaxWeightTotal > 0) tMaxWeightedSum.div(tMaxWeightTotal) else null,
            nighttimeTemperature = if (tMinWeightTotal > 0) tMinWeightedSum.div(tMinWeightTotal) else null
        )
    }

    /*
     * Models the weight for each nearby station after the normal distribution.
     * Let μ = 0
     *     σ = 1 representing an arbitrary distance of 20km
     *     x = distance between station and location in multiples of 20km
     * Illustrative weights at various distances:
     *  - Station at location: 0.399 (100% weight)
     *  - Station 20km away:   0.242 (60% weight)
     *  - Station 40km away:   0.054 (14% weight)
     *  - Station 60km away:   0.004 (1% weight)
     * Source: https://en.wikipedia.org/wiki/Normal_distribution
     */
    private fun getWeight(distance: Double): Double {
        val sigmaDistance = DISTANCE_LIMIT / 3.0
        val x = distance / sigmaDistance
        val weight = 1.0 / sqrt(2.0 * PI) * exp(-x.pow(2.0) / 2.0)
        return weight
    }

    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        if (coordinatesChanged) return true
        val stations = location.parameters.getOrElse(id) { null }?.getOrElse("stations") { null }
        return stations.isNullOrEmpty()
    }

    override fun requestLocationParameters(context: Context, location: Location): Observable<Map<String, String>> {
        // set a bbox of 120km x 120km (60km to each cardinal direction)
        // TODO: Handle wraparounds for locations near the IDL (180° longitude) more gracefully
        val north = min(location.latitude + DISTANCE_LIMIT / (2 * PI * EARTH_POLAR_RADIUS) * 360, 90.0)
        val south = max(location.latitude - DISTANCE_LIMIT / (2 * PI * EARTH_POLAR_RADIUS) * 360, -90.0)
        val multiple = cos(location.latitude / 180 * PI)
        val east = if (multiple != 0.0) {
            min(location.longitude + DISTANCE_LIMIT / (2 * PI * EARTH_EQUATORIAL_RADIUS * multiple) * 360, 180.0)
        } else {
            180.0
        }
        val west = if (multiple != 0.0) {
            max(location.longitude - DISTANCE_LIMIT / (2 * PI * EARTH_EQUATORIAL_RADIUS * multiple) * 360, -180.0)
        } else {
            -180.0
        }
        val bbox = "$north,$west,$south,$east"

        val finalYear = (Date().toCalendarWithTimeZone(location.javaTimeZone)[Calendar.YEAR]).toDouble()
            .roundDownToNearestMultiplier(10.0).roundToInt()
        val initialYear = finalYear - 29

        return mApi.getStations(
            bbox = bbox,
            startDate = "$initialYear-01-01T00:00:00",
            endDate = "$finalYear-12-31T23:59:59"
        ).map {
            // The nearest station from a location may not have the most complete historical weather record.
            // Therefore we will obtain and store all the stations within the 120km x 120km area,
            // and apply weighting to records based on their respective station distance.
            val stationMap = mutableMapOf<String, Double>()
            it.results?.forEach { results ->
                results.stations.getOrNull(0)?.let { station ->
                    stationMap[station.id] = SphericalUtil.computeDistanceBetween(
                        LatLng(location.latitude, location.longitude),
                        LatLng(results.centroid.point[1], results.centroid.point[0])
                    )
                }
            }
            buildMap {
                put("stations", Json.encodeToString(stationMap))
            }
        }
    }

    companion object {
        private const val NCEI_BASE_URL = "https://www.ncei.noaa.gov/"
        private const val EARTH_POLAR_RADIUS = 6356752
        private const val EARTH_EQUATORIAL_RADIUS = 6378137
        const val DISTANCE_LIMIT = 60000
    }
}
