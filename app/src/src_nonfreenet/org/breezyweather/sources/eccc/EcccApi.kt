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

package org.breezyweather.sources.eccc

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.eccc.json.EcccResult
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface EcccApi {
    @GET("v3/{lang}/Location/{lat},{lon}")
    fun getForecast(
        @Header("User-Agent") userAgent: String,
        @Header("x-api-key") apiKey: String,
        @Path("lang") lang: String,
        @Path("lat") lat: Double,
        @Path("lon") lon: Double,
    ): Observable<List<EcccResult>>
}
