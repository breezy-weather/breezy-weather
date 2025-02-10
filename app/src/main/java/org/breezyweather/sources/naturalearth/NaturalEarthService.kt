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

package org.breezyweather.sources.naturalearth

import android.content.Context
import android.os.Build
import breezyweather.domain.location.model.Location
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import com.google.maps.android.data.geojson.GeoJsonFeature
import com.google.maps.android.data.geojson.GeoJsonMultiPolygon
import com.google.maps.android.data.geojson.GeoJsonParser
import com.google.maps.android.data.geojson.GeoJsonPoint
import com.google.maps.android.data.geojson.GeoJsonPolygon
import com.google.maps.android.model.LatLng
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.R
import org.breezyweather.common.extensions.codeForNaturalEarthService
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.utils.helpers.LogHelper
import org.json.JSONObject
import javax.inject.Inject

/**
 * Natural Earth reverse geocoding
 *
 * Offline source, based on public domain data
 * 1:50m Cultural Vectors files downloaded from:
 * https://www.naturalearthdata.com/downloads/50m-cultural-vectors/
 * We can’t take 1:110m files because they don’t include all small islands/countries
 *
 * Latest updates used:
 * ne_50m_admin_0_countries.shp v5.1.1
 *
 * https://mapshaper.org/ was used to convert to GeoJSON
 * TODO: It would be best to make our own converter so that we can exclude features we don’t want and
 * make the geojson file more lightweight
 */
class NaturalEarthService @Inject constructor() : ReverseGeocodingSource {

    override val id = "naturalearth"
    override val name = "Natural Earth"
    override val reverseGeocodingAttribution = name

    private fun getMatchingFeaturesForLocation(
        context: Context,
        file: Int,
        location: Location,
    ): List<GeoJsonFeature> {
        val text = context.resources.openRawResource(file)
            .bufferedReader().use { it.readText() }
        val geoJsonParser = GeoJsonParser(JSONObject(text))

        return geoJsonParser.features.filter { feature ->
            when (feature.geometry) {
                is GeoJsonPolygon -> (feature.geometry as GeoJsonPolygon).coordinates.any { polygon ->
                    PolyUtil.containsLocation(location.latitude, location.longitude, polygon, true)
                }
                is GeoJsonMultiPolygon -> (feature.geometry as GeoJsonMultiPolygon).polygons.any {
                    it.coordinates.any { polygon ->
                        PolyUtil.containsLocation(location.latitude, location.longitude, polygon, true)
                    }
                }
                is GeoJsonPoint -> SphericalUtil.computeDistanceBetween(
                    LatLng(location.latitude, location.longitude),
                    (feature.geometry as GeoJsonPoint).coordinates
                ) < 50000 // 50 km circle around center point, it’s arbitrary and may need to be adjusted
                else -> false
            }
        }
    }

    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location,
    ): Observable<List<Location>> {
        val locationList = mutableListOf<Location>()
        val languageCode = context.currentLocale.codeForNaturalEarthService

        // Countries
        val matchingCountries = getMatchingFeaturesForLocation(context, R.raw.ne_50m_admin_0_countries, location)
        if (matchingCountries.size != 1) {
            locationList.add(location)
            LogHelper.log(
                msg = "[NaturalEarthService] Reverse geocoding skipped: ${matchingCountries.size} matching results"
            )
            return Observable.just(locationList)
        }

        locationList.add(
            location.copy(
                country = matchingCountries[0].getProperty("NAME_$languageCode")
                    ?: matchingCountries[0].getProperty("NAME_LONG")
                    ?: "",
                countryCode = matchingCountries[0].getProperty("ISO_A2"),
                // Make sure to update TimeZone, especially useful on current location
                timeZone = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    android.icu.util.TimeZone.getDefault().id
                } else {
                    java.util.TimeZone.getDefault().id
                }
            )
        )
        return Observable.just(locationList)
    }
}
