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

package org.breezyweather.sources.knmi

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.knmi.json.KnmiWeather
import org.breezyweather.sources.knmi.json.KnmiWeatherAlerts
import org.breezyweather.sources.knmi.json.KnmiWeatherDetail
import org.breezyweather.sources.knmi.json.KnmiWeatherSnapshot
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * See https://apis.developer.overheid.nl/apis/knmi-app-api/specificatie/production
 */
interface KnmiApi {
    @GET("alerts")
    fun getAlerts(): Observable<KnmiWeatherAlerts>

    @GET("weather")
    fun getWeather(
        @Query("location") location: Int,
        @Query("region") region: Int?,
    ): Observable<KnmiWeather>

    @GET("weather/detail")
    fun getWeatherDetail(
        @Query("location") location: Int,
        @Query("region") region: Int?,
        @Query("date") date: String,
    ): Observable<KnmiWeatherDetail>

    @GET("weather/snapshot")
    fun getWeatherSnapshot(
        @Query("location") location: Int,
        @Query("region") region: Int?,
    ): Observable<KnmiWeatherSnapshot>
}
