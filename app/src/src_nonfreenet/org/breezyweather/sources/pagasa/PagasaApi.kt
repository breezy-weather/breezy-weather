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

package org.breezyweather.sources.pagasa

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.pagasa.json.PagasaCurrentResult
import org.breezyweather.sources.pagasa.json.PagasaHourlyResult
import org.breezyweather.sources.pagasa.json.PagasaLocationResult
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface PagasaApi {
    // Not a reliable source of current weather despite the URL
    @Headers("Origin: https://www.pagasa.dost.gov.ph")
    @POST("api/CurrentWeather")
    fun getLocations(): Observable<Map<String, PagasaLocationResult>>

    @Headers("Origin: https://www.pagasa.dost.gov.ph")
    @POST("api/NearestAWS")
    fun getCurrent(): Observable<Map<String, List<PagasaCurrentResult>>>

    @GET("api/meteogram/{site}/{day}")
    fun getHourly(
        @Path("site") site: String,
        @Path("day") day: Int,
    ): Observable<PagasaHourlyResult>
}
