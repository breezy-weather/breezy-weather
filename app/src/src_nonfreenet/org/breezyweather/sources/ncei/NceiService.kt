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
import breezyweather.domain.weather.wrappers.WeatherWrapper
import com.google.maps.android.SphericalUtil
import com.google.maps.android.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import kotlinx.serialization.json.Json
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.WeatherSource
import retrofit2.Retrofit
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class NceiService @Inject constructor(
    @ApplicationContext context: Context,
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

    override val testingLocations: List<Location> = emptyList()

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val month = Date().toCalendarWithTimeZone(location.javaTimeZone)[Calendar.MONTH] + 1
        val finalYear = floor(Date().toCalendarWithTimeZone(location.javaTimeZone)[Calendar.YEAR] / 10.0) * 10.0
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
                startDate = "${initialYear.toInt()}-01-01",
                endDate = "${finalYear.toInt()}-12-31"
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

        val finalYear = floor(Date().toCalendarWithTimeZone(location.javaTimeZone)[Calendar.YEAR] / 10.0) * 10.0
        val initialYear = finalYear - 29

        return mApi.getStations(
            bbox = bbox,
            startDate = "${initialYear.toInt()}-01-01T00:00:00",
            endDate = "${finalYear.toInt()}-12-31T23:59:59"
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
