/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.sources.pirateweather

import retrofit2.http.GET
import retrofit2.http.Query
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.pirateweather.json.PirateWeatherForecastResult
import retrofit2.http.Path

/**
 * See https://docs.pirateweather.net/en/latest/Specification/
 */
interface PirateWeatherApi {
    @GET("forecast/{apikey}/{lat},{lon}")
    fun getForecast(
        @Path("apikey") apikey: String,
        @Path("lat") lat: Float,
        @Path("lon") lon: Float,
        @Query("units") units: String,
        @Query("lang") lang: String
    ): Observable<PirateWeatherForecastResult>
}