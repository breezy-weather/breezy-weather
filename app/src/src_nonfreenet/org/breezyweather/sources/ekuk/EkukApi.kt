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

package org.breezyweather.sources.ekuk

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.ekuk.json.EkukObservationsResult
import org.breezyweather.sources.ekuk.json.EkukStationsResult
import retrofit2.http.GET
import retrofit2.http.Query

interface EkukApi {
    @GET("api/station/en")
    fun getStations(): Observable<EkukStationsResult>

    @GET("api/monitoring/en")
    fun getObservations(
        @Query("stations") station: String,
        @Query("indicators") indicators: String = "",
        @Query("range") range: String,
    ): Observable<List<EkukObservationsResult>>
}
