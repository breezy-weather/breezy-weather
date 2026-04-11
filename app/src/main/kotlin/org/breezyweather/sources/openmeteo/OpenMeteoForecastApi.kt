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

package org.breezyweather.sources.openmeteo

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.openmeteo.json.OpenMeteoWeatherResult
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Open-Meteo API
 */
interface OpenMeteoForecastApi {
    @GET("v1/forecast?timezone=auto&timeformat=unixtime")
    fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("models") models: String = OpenMeteoWeatherModel.BEST_MATCH.id,
        @Query("daily") daily: String,
        @Query("hourly") hourly: String,
        @Query("minutely_15") minutely15: String,
        @Query("current") current: String,
        @Query("forecast_days") forecastDays: Int,
        @Query("past_days") pastDays: Int,
        @Query("windspeed_unit") windspeedUnit: String,
    ): Observable<OpenMeteoWeatherResult>
}
