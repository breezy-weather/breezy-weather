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

package org.breezyweather.sources.nominatim

import android.content.Context
import breezyweather.domain.location.model.LocationAddressInfo
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationSearchSource
import org.breezyweather.common.source.ReverseGeocodingSource
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

/**
 * Nominatim service
 *
 * Search is not possible, as timezone is mandatory
 * Only supports reverse geocoding for current location, by falling back to device timezone
 */
class NominatimService @Inject constructor(
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), LocationSearchSource, ReverseGeocodingSource {

    override val id = "nominatim"
    override val name = "Nominatim"
    override val locationSearchAttribution =
        "Nominatim • Data © OpenStreetMap contributors, ODbL 1.0. https://osm.org/copyright"
    override val privacyPolicyUrl = "https://osmfoundation.org/wiki/Privacy_Policy"
    override val continent = SourceContinent.WORLDWIDE

    override val supportedFeatures = mapOf(
        SourceFeature.REVERSE_GEOCODING to locationSearchAttribution
    )
    override val attributionLinks = mapOf(
        name to NOMINATIM_BASE_URL,
        "OpenStreetMap" to "https://osm.org/",
        "https://osm.org/copyright" to "https://osm.org/copyright"
    )
    private val mApi by lazy {
        client
            .baseUrl(NOMINATIM_BASE_URL)
            .build()
            .create(NominatimApi::class.java)
    }

    override fun requestLocationSearch(
        context: Context,
        query: String,
    ): Observable<List<LocationAddressInfo>> {
        return mApi.searchLocations(
            userAgent = USER_AGENT,
            q = query,
            limit = 20
        ).map { results ->
            results.mapNotNull {
                if (it.address?.countryCode == null || it.address.countryCode.isEmpty()) {
                    null
                } else {
                    LocationAddressInfo(
                        latitude = it.lat.toDoubleOrNull(),
                        longitude = it.lon.toDoubleOrNull(),
                        district = it.address.village,
                        city = it.address.town ?: it.name,
                        cityCode = it.placeId?.toString(),
                        admin3 = it.address.municipality,
                        admin2 = it.address.county,
                        admin1 = it.address.state,
                        country = it.address.country,
                        countryCode = it.address.countryCode
                    )
                }
            }
        }
    }

    override fun requestNearestLocation(
        context: Context,
        latitude: Double,
        longitude: Double,
    ): Observable<List<LocationAddressInfo>> {
        return mApi.getReverseLocation(
            userAgent = USER_AGENT,
            lat = latitude,
            lon = longitude
        ).map {
            if (it.address?.countryCode == null) {
                throw InvalidLocationException()
            }

            listOf(
                LocationAddressInfo(
                    latitude = it.lat.toDoubleOrNull(),
                    longitude = it.lon.toDoubleOrNull(),
                    district = it.address.village,
                    city = it.address.town ?: it.name,
                    cityCode = it.placeId?.toString(),
                    admin3 = it.address.municipality,
                    admin2 = it.address.county,
                    admin1 = it.address.state,
                    country = it.address.country,
                    countryCode = it.address.countryCode
                )
            )
        }
    }

    companion object {
        private const val NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org/"
        private const val USER_AGENT =
            "BreezyWeather/${BuildConfig.VERSION_NAME} github.com/breezy-weather/breezy-weather/issues"
    }
}
