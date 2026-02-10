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

package org.breezyweather.sources.atmo

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.atmo.json.AtmoFrancePollenResult
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

/**
 * API
 */
interface AtmoFranceApi {

    @GET(
        "data/{api_code}/{\"code_zone\":{\"operator\":\"=\",\"value\":\"{code_insee}\"},\"date_ech\":{\"operator\":\"=\",\"value\":\"{date_ech}\"}}"
    )
    fun getPollen(
        @Header("Api-token") apiToken: String,
        @Header("User-Agent") userAgent: String,
        @Path("api_code") apiCode: Int,
        @Path("code_insee") codeInsee: String,
        @Path("date_ech") dateEch: String,
    ): Observable<AtmoFrancePollenResult>
}
