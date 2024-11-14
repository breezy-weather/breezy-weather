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

package org.breezyweather.sources.metoffice

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.metoffice.json.MetOfficeDaily
import org.breezyweather.sources.metoffice.json.MetOfficeForecast
import org.breezyweather.sources.metoffice.json.MetOfficeHourly
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * See https://datahub.metoffice.gov.uk/docs/f/category/site-specific/type/site-specific/api-documentation
 */
interface MetOfficeApi {
    @GET("point/hourly")
    fun getHourlyForecast(
        @Header("apikey") apikey: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("includeLocationName") includeLocationName: Boolean = false,
        @Query("dataSource") dataSource: String = "BD1",
    ): Observable<MetOfficeForecast<MetOfficeHourly>>

    @GET("point/daily")
    fun getDailyForecast(
        @Header("apikey") apikey: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("includeLocationName") includeLocationName: Boolean = false,
        @Query("dataSource") dataSource: String = "BD1",
    ): Observable<MetOfficeForecast<MetOfficeDaily>>
}
