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

package org.breezyweather.sources.nominatim

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.nominatim.json.NominatimLocationResult
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * Nominatim API
 */
interface NominatimApi {

    @GET("search")
    fun searchLocations(
        @Header("Accept-Language") acceptLanguage: String,
        @Header("User-Agent") userAgent: String,
        @Query("q") q: String,
        @Query("limit") limit: Int = 10,
        @Query("featureType") featureType: String = "city",
        @Query("format") format: String = "jsonv2",
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("key") key: String? = null,
    ): Observable<List<NominatimLocationResult>>

    @GET("reverse")
    fun getReverseLocation(
        @Header("Accept-Language") acceptLanguage: String,
        @Header("User-Agent") userAgent: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("zoom") zoom: Int = 13,
        @Query("format") format: String = "jsonv2",
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("key") key: String? = null,
    ): Observable<NominatimLocationResult>
}
