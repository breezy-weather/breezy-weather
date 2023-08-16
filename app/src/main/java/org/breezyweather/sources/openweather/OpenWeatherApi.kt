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

package org.breezyweather.sources.openweather

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.openweather.json.OpenWeatherAirPollutionResult
import org.breezyweather.sources.openweather.json.OpenWeatherOneCallResult
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * OpenWeather API.
 */
interface OpenWeatherApi {

    // Contains current weather, minute forecast for 1 hour, hourly forecast for 48 hours, daily forecast for 7 days (8 for 3.0) and government weather alerts
    @GET("data/{version}/onecall")
    fun getOneCall(
        @Path("version") version: String,
        @Query("appid") apikey: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String,
        @Query("lang") lang: String
    ): Observable<OpenWeatherOneCallResult>

    @GET("data/2.5/air_pollution/forecast")
    fun getAirPollution(
        @Query("appid") apikey: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Observable<OpenWeatherAirPollutionResult>
}
