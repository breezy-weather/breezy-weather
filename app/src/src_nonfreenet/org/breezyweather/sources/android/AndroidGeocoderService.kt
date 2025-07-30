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

package org.breezyweather.sources.android

import android.content.Context
import android.location.Geocoder
import android.os.Build
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceFeature
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.rx3.rxObservable
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.source.ReverseGeocodingSource
import javax.inject.Inject

class AndroidGeocoderService @Inject constructor() : ReverseGeocodingSource {

    override val id = "nativegeocoder"
    override val name = "Android"

    override val supportedFeatures = mapOf(
        SourceFeature.REVERSE_GEOCODING to name
    )

    override fun isFeatureSupportedForLocation(location: Location, feature: SourceFeature): Boolean {
        return Geocoder.isPresent()
    }

    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location,
    ): Observable<List<Location>> {
        val geocoder = Geocoder(context, context.currentLocale)
        return rxObservable {
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

            val locationList = mutableListOf<Location>()
            addresses?.getOrNull(0)?.let {
                locationList.add(
                    location.copy(
                        city = it.locality,
                        district = it.subLocality,
                        admin1 = it.adminArea,
                        admin2 = it.subAdminArea,
                        country = it.countryName,
                        countryCode = it.countryCode,
                        // Make sure to update TimeZone in case the user moved
                        timeZone = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            android.icu.util.TimeZone.getDefault().id
                        } else {
                            java.util.TimeZone.getDefault().id
                        }
                    )
                )
            }
            send(locationList)
        }
    }
}
