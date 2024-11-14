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

package org.breezyweather.sources.nws

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.nws.json.NwsAlertsResult
import org.breezyweather.sources.nws.json.NwsGridPointResult
import org.breezyweather.sources.nws.json.NwsPointResult
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface NwsApi {
    @GET("points/{lat},{lon}")
    fun getPoints(
        @Header("User-Agent") userAgent: String,
        @Path("lat") lat: Double,
        @Path("lon") lon: Double,
    ): Observable<NwsPointResult>

    @GET("gridpoints/{gridId}/{gridX},{gridY}")
    fun getForecast(
        @Header("User-Agent") userAgent: String,
        @Path("gridId") gridId: String,
        @Path("gridX") gridX: Int,
        @Path("gridY") gridY: Int,
    ): Observable<NwsGridPointResult>

    @GET("alerts/active")
    fun getActiveAlerts(
        @Header("User-Agent") userAgent: String,
        @Query("point") point: String, // Format: lat,lon
    ): Observable<NwsAlertsResult>
}
