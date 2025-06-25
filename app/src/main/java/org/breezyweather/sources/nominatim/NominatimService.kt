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
import android.os.Build
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.source.HttpSource
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
) : HttpSource(), ReverseGeocodingSource {

    override val id = "nominatim"
    override val name = "Nominatim"
    override val reverseGeocodingAttribution =
        "Nominatim · Data © OpenStreetMap contributors, ODbL 1.0. https://osm.org/copyright"
    override val privacyPolicyUrl = "https://osmfoundation.org/wiki/Privacy_Policy"
    override val continent = SourceContinent.WORLDWIDE

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

    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location,
    ): Observable<List<Location>> {
        return mApi.getReverseLocation(
            userAgent = USER_AGENT,
            lat = location.latitude,
            lon = location.longitude
        ).map {
            val locationList = mutableListOf<Location>()
            locationList.add(
                location.copy(
                    cityId = it.placeId?.toString(),
                    district = it.address?.village,
                    city = it.address?.town ?: it.name,
                    admin3 = it.address?.municipality,
                    admin2 = it.address?.county,
                    admin1 = it.address?.state,
                    country = it.address?.country.let { country ->
                        if (country.isNullOrEmpty()) location.country else country
                    },
                    countryCode = it.address?.countryCode.let { countryCode ->
                        if (countryCode.isNullOrEmpty()) {
                            location.countryCode
                        } else {
                            countryCode.uppercase(context.currentLocale)
                        }
                    },
                    // Make sure to update TimeZone, especially useful on current location
                    timeZone = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        android.icu.util.TimeZone.getDefault().id
                    } else {
                        java.util.TimeZone.getDefault().id
                    }
                )
            )
            locationList
        }
    }

    companion object {
        private const val NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org/"
        private const val USER_AGENT =
            "BreezyWeather/${BuildConfig.VERSION_NAME} github.com/breezy-weather/breezy-weather/issues"
    }
}
