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

package org.breezyweather.sources.imgw

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.imgw.json.forecast.ImgwForecastResult
import retrofit2.http.GET
import retrofit2.http.Query

interface ImgwForecastApi {
    @GET("/api/v1/forecast/fcapi")
    fun getForecast(
        @Query("token") token: String,
        @Query("lat") latitude: String,
        @Query("lon") longitude: String,
        @Query("m") model: String = "hybrid",
    ): Observable<ImgwForecastResult>

    @GET("/api/v1/forecast/forecastapi")
    fun getGfsForecast(
        @Query("token") token: String,
        @Query("lat") latitude: String,
        @Query("lon") longitude: String,
    ): Observable<ImgwForecastResult>
}
