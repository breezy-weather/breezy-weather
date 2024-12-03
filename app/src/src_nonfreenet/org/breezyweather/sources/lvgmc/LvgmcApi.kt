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

package org.breezyweather.sources.lvgmc

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.lvgmc.json.LvgmcAirQualityLocationResult
import org.breezyweather.sources.lvgmc.json.LvgmcAirQualityResult
import org.breezyweather.sources.lvgmc.json.LvgmcCurrentLocation
import org.breezyweather.sources.lvgmc.json.LvgmcCurrentResult
import org.breezyweather.sources.lvgmc.json.LvgmcForecastResult
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface LvgmcApi {
    @GET("data/weather_points_forecast")
    fun getForecastLocations(
        @Query("laiks") laiks: String,
        @Query("bounds") bounds: String,
    ): Observable<List<LvgmcForecastResult>>

    @GET("data/weather_monitoring_points")
    fun getCurrentLocations(): Observable<List<LvgmcCurrentLocation>>

    @GET("data/na_atmosfera_stacijas")
    fun getAirQualityLocations(): Observable<List<LvgmcAirQualityLocationResult>>

    @GET("data/weather_forecast_for_location_{scope}")
    fun getForecast(
        @Path("scope") scope: String,
        @Query("punkts") punkts: String,
    ): Observable<List<LvgmcForecastResult>>

    @GET("data/weather_monitoring_data_raw")
    fun getCurrent(): Observable<List<LvgmcCurrentResult>>

    @GET("data/gaisa_kvalitate_envista_batch")
    fun getAirQuality(
        @Query("stacija_id") station: String,
        @Query("no_datums") fromDate: String,
    ): Observable<List<LvgmcAirQualityResult>>
}
