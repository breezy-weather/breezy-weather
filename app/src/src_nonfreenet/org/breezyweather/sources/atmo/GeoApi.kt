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

package org.breezyweather.sources.atmo

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.atmo.json.GeoResult
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API
 */
interface GeoApi {

    @GET("reverse")
    fun getReverseAddress(
        @Query("lon") longitude: Double,
        @Query("lat") latitude: Double,
    ): Observable<GeoResult>
}
