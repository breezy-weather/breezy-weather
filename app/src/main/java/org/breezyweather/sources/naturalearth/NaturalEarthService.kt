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
import breezyweather.domain.location.model.Location
import com.google.maps.android.PolyUtil
import com.google.maps.android.data.geojson.GeoJsonFeature
import com.google.maps.android.data.geojson.GeoJsonMultiPolygon
import com.google.maps.android.data.geojson.GeoJsonParser
import com.google.maps.android.data.geojson.GeoJsonPolygon
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.R
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.settings.SettingsManager
import org.json.JSONObject
import javax.inject.Inject

/**
 * Natural Earth reverse geocoding
 *
 * Offline source, based on public domain data
 * 1:110m Cultural Vectors files downloaded from:
 * https://www.naturalearthdata.com/downloads/110m-cultural-vectors/
 *
 * Latest updates used:
 * ne_110m_admin_0_countries.shp v5.1.1
 *
 * https://mapshaper.org/ was used to convert to GeoJSON, then manually edited to remove
 * Antarctica because we have issues with it
 * It would be best to make our own converter so that we can exclude features we don’t want
 */
class NaturalEarthService @Inject constructor() : ReverseGeocodingSource {

    override val id = "naturalearth"
    override val name = "Natural Earth"

    private fun getMatchingFeaturesForLocation(
        context: Context, file: Int, location: Location
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
                else -> false
            }
        }
    }

    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location
    ): Observable<List<Location>> {
        val locationList: MutableList<Location> = ArrayList()
        val languageCode = SettingsManager.getInstance(context).language.codeForNaturalEarthService

        // Countries
        val matchingCountries = getMatchingFeaturesForLocation(
            context, R.raw.ne_110m_admin_0_countries, location
        )

        if (matchingCountries.size != 1) {
            locationList.add(location)
            return Observable.just(locationList)
        }

        locationList.add(
            location.copy(
                country = matchingCountries[0].getProperty("NAME_$languageCode")
                    ?: matchingCountries[0].getProperty("NAME")
                    ?: matchingCountries[0].getProperty("NAME_EN"),
                countryCode = matchingCountries[0].getProperty("ISO_A2")
            )
        )
        return Observable.just(locationList)
    }

    // CONFIG
    // Implement setting to switch between de facto vs de jure boundaries
    //private val config = SourceConfigStore(context, id)

}