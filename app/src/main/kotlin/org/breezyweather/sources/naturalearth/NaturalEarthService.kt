/*
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
import breezyweather.domain.location.model.LocationAddressInfo
import breezyweather.domain.source.SourceFeature
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import com.google.maps.android.data.Feature
import com.google.maps.android.data.geojson.GeoJsonMultiPolygon
import com.google.maps.android.data.geojson.GeoJsonParser
import com.google.maps.android.data.geojson.GeoJsonPoint
import com.google.maps.android.data.geojson.GeoJsonPolygon
import com.google.maps.android.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.R
import org.breezyweather.common.extensions.codeForNaturalEarth
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.extensions.parseRawGeoJson
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.utils.helpers.LogHelper
import org.breezyweather.sources.RefreshHelper
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
 *  make the geojson file more lightweight
 */
class NaturalEarthService @Inject constructor(
    @ApplicationContext context: Context,
) : ReverseGeocodingSource {

    override val id = "naturalearth"
    override val name = "Natural Earth"

    private val geoJsonParser: GeoJsonParser by lazy {
        context.parseRawGeoJson(R.raw.ne_50m_admin_0_countries)
    }

    override val supportedFeatures = mapOf(
        SourceFeature.REVERSE_GEOCODING to name
    )

    override fun requestNearestLocation(
        context: Context,
        latitude: Double,
        longitude: Double,
    ): Observable<List<LocationAddressInfo>> {
        val languageCode = context.currentLocale.codeForNaturalEarth

        val matchingCountries = geoJsonParser.features.filter { isMatchingFeature(it, latitude, longitude) }
        if (matchingCountries.size != 1) {
            LogHelper.log(
                msg = "[NaturalEarthService] Reverse geocoding skipped: ${matchingCountries.size} matching results"
            )
            return Observable.just(emptyList())
        }

        val countryCode = matchingCountries[0].getProperty("ISO_A2") ?: ""
        return Observable.just(
            listOf(
                LocationAddressInfo(
                    country = countryCode.takeIf { it.isNotEmpty() }
                        ?.let { context.currentLocale.getCountryName(it) }
                        ?: matchingCountries[0].getProperty("NAME_$languageCode")
                        ?: matchingCountries[0].getProperty("NAME_LONG"),
                    countryCode = countryCode
                )
            )
        )
    }

    private fun isMatchingFeature(
        feature: Feature,
        latitude: Double,
        longitude: Double,
    ): Boolean {
        return when (feature.geometry) {
            is GeoJsonPolygon -> (feature.geometry as GeoJsonPolygon).coordinates.any { polygon ->
                PolyUtil.containsLocation(latitude, longitude, polygon, true)
            }
            is GeoJsonMultiPolygon -> (feature.geometry as GeoJsonMultiPolygon).polygons.any {
                it.coordinates.any { polygon ->
                    PolyUtil.containsLocation(latitude, longitude, polygon, true)
                }
            }
            is GeoJsonPoint -> SphericalUtil.computeDistanceBetween(
                LatLng(latitude, longitude),
                (feature.geometry as GeoJsonPoint).coordinates
            ) < RefreshHelper.REVERSE_GEOCODING_DISTANCE_LIMIT
            else -> false
        }
    }

    // No ambiguous codes
    override val knownAmbiguousCountryCodes: Array<String> = emptyArray()
}
