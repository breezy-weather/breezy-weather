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
import breezyweather.domain.location.model.Location
import breezyweather.domain.location.model.LocationAddressInfo
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

    override fun requestNearestLocation(
        context: Context,
        location: Location,
    ): Observable<List<LocationAddressInfo>> {
        val geocoder = Geocoder(context, context.currentLocale)
        return rxObservable {
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

            val locationList = mutableListOf<LocationAddressInfo>()
            addresses?.getOrNull(0)?.let {
                locationList.add(
                    LocationAddressInfo(
                        city = it.locality,
                        district = it.subLocality,
                        admin1 = it.adminArea,
                        admin2 = it.subAdminArea,
                        country = it.countryName,
                        countryCode = it.countryCode
                    )
                )
            }
            send(locationList)
        }
    }
}
