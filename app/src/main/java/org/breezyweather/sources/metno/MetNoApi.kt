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

package org.breezyweather.sources.metno

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.metno.json.MetNoAirQualityResult
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import org.breezyweather.sources.metno.json.MetNoForecastResult
import org.breezyweather.sources.metno.json.MetNoMoonResult
import org.breezyweather.sources.metno.json.MetNoNowcastResult
import org.breezyweather.sources.metno.json.MetNoSunResult

/**
 * MET Norway Weather API.
 */
interface MetNoApi {
    @GET("locationforecast/2.0/complete.json")
    fun getForecast(
        @Header("User-Agent") userAgent: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Observable<MetNoForecastResult>

    @GET("sunrise/3.0/sun")
    fun getSun(
        @Header("User-Agent") userAgent: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("date") date: String
    ): Observable<MetNoSunResult>

    @GET("sunrise/3.0/moon")
    fun getMoon(
        @Header("User-Agent") userAgent: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("date") date: String
    ): Observable<MetNoMoonResult>

    // Only available in Nordic area
    @GET("nowcast/2.0/complete.json")
    fun getNowcast(
        @Header("User-Agent") userAgent: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Observable<MetNoNowcastResult>

    @GET("airqualityforecast/0.1/")
    fun getAirQuality(
        @Header("User-Agent") userAgent: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Observable<MetNoAirQualityResult>

    /*@GET("metalerts/1.1/")
    fun getAlerts(
        @Header("User-Agent") userAgent: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Observable<List<MetNoAlertResult>>*/
}