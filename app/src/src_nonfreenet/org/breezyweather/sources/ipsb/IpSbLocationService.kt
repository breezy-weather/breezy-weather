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

package org.breezyweather.sources.ipsb

import android.content.Context
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.exceptions.InvalidOrIncompleteDataException
import org.breezyweather.common.rxjava.SchedulerTransformer
import org.breezyweather.common.source.LocationPositionWrapper
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

class IpSbLocationService @Inject constructor(
    @Named("JsonClient") client: Retrofit.Builder,
) : IpSbLocationServiceStub() {

    override val privacyPolicyUrl = "https://ip.sb/privacy-policy/"

    private val mApi by lazy {
        client
            .baseUrl(IP_SB_BASE_URL)
            .build()
            .create(IpSbLocationApi::class.java)
    }

    override fun requestLocation(context: Context): Observable<LocationPositionWrapper> {
        return mApi.getLocation()
            .compose(SchedulerTransformer.create())
            .map { t ->
                if (t.longitude == 0.0 && t.latitude == 0.0) {
                    throw InvalidOrIncompleteDataException()
                }
                LocationPositionWrapper(
                    latitude = t.latitude,
                    longitude = t.longitude
                )
            }
    }

    companion object {
        private const val IP_SB_BASE_URL = "https://api.ip.sb/"
    }
}
