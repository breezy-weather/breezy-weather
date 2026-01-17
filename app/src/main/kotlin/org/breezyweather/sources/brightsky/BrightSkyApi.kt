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

package org.breezyweather.sources.brightsky

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.brightsky.json.BrightSkyAlertsResult
import org.breezyweather.sources.brightsky.json.BrightSkyCurrentWeatherResult
import org.breezyweather.sources.brightsky.json.BrightSkyWeatherResult
import retrofit2.http.GET
import retrofit2.http.Query

interface BrightSkyApi {
    @GET("weather")
    fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("date") date: String,
        @Query("last_date") lastDate: String,
    ): Observable<BrightSkyWeatherResult>

    @GET("current_weather")
    fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
    ): Observable<BrightSkyCurrentWeatherResult>

    @GET("alerts")
    fun getAlerts(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
    ): Observable<BrightSkyAlertsResult>
}
