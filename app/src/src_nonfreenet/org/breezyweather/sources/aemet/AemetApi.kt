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

package org.breezyweather.sources.aemet

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.aemet.json.AemetApiResult
import org.breezyweather.sources.aemet.json.AemetCurrentResult
import org.breezyweather.sources.aemet.json.AemetDailyResult
import org.breezyweather.sources.aemet.json.AemetHourlyResult
import org.breezyweather.sources.aemet.json.AemetNormalsResult
import org.breezyweather.sources.aemet.json.AemetStationsResult
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface AemetApi {
    @GET("api/valores/climatologicos/inventarioestaciones/todasestaciones")
    fun getStationsUrl(
        @Header("api_key") apiKey: String,
    ): Observable<AemetApiResult>

    @GET("{path}")
    fun getStations(
        @Header("api_key") apiKey: String,
        @Path("path", encoded = true) path: String,
    ): Observable<List<AemetStationsResult>>

    @GET("api/prediccion/especifica/municipio/{range}/{municipio}")
    fun getForecastUrl(
        @Header("api_key") apiKey: String,
        @Path("range") range: String,
        @Path("municipio") municipio: String,
    ): Observable<AemetApiResult>

    @GET("{path}")
    fun getHourly(
        @Header("api_key") apiKey: String,
        @Path("path", encoded = true) path: String,
    ): Observable<List<AemetHourlyResult>>

    @GET("{path}")
    fun getDaily(
        @Header("api_key") apiKey: String,
        @Path("path", encoded = true) path: String,
    ): Observable<List<AemetDailyResult>>

    @GET("api/observacion/convencional/datos/estacion/{estacion}")
    fun getCurrentUrl(
        @Header("api_key") apiKey: String,
        @Path("estacion") estacion: String,
    ): Observable<AemetApiResult>

    @GET("{path}")
    fun getCurrent(
        @Header("api_key") apiKey: String,
        @Path("path", encoded = true) path: String,
    ): Observable<List<AemetCurrentResult>>

    @GET("api/valores/climatologicos/normales/estacion/{estacion}")
    fun getNormalsUrl(
        @Header("api_key") apiKey: String,
        @Path("estacion") estacion: String,
    ): Observable<AemetApiResult>

    @GET("{path}")
    fun getNormals(
        @Header("api_key") apiKey: String,
        @Path("path", encoded = true) path: String,
    ): Observable<List<AemetNormalsResult>>
}
