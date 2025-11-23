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

package org.breezyweather.sources.accu

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.accu.json.AccuAlertResult
import org.breezyweather.sources.accu.json.AccuCurrentResult
import org.breezyweather.sources.accu.json.AccuForecastDailyResult
import org.breezyweather.sources.accu.json.AccuForecastHourlyResult
import org.breezyweather.sources.accu.json.AccuLocationResult
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Accu api.
 */
interface AccuDeveloperApi {
    @GET("locations/v1/translate")
    fun getWeatherLocation(
        @Query("apikey") apikey: String,
        @Query("q") q: String,
        @Query("language") language: String,
        @Query("details") details: Boolean,
        @Query("alias") alias: String,
    ): Observable<List<AccuLocationResult>>

    @GET("locations/v1/cities/geoposition/search")
    fun getWeatherLocationByGeoPosition(
        @Query("apikey") apikey: String,
        @Query("language") language: String,
        @Query("details") details: Boolean,
        @Query("q") q: String,
    ): Observable<AccuLocationResult>

    @GET("currentconditions/v1/{city_key}")
    fun getCurrent(
        @Path("city_key") cityKey: String,
        @Query("apikey") apikey: String,
        @Query("language") language: String,
        @Query("details") details: Boolean,
    ): Observable<List<AccuCurrentResult>>

    @GET("forecasts/v1/daily/{days}day/{city_key}")
    fun getDaily(
        @Path("days") days: String,
        @Path("city_key") cityKey: String,
        @Query("apikey") apikey: String,
        @Query("language") language: String,
        @Query("details") details: Boolean,
        @Query("metric") metric: Boolean,
    ): Observable<AccuForecastDailyResult>

    @GET("forecasts/v1/hourly/{hours}hour/{city_key}")
    fun getHourly(
        @Path("hours") hours: String,
        @Path("city_key") cityKey: String,
        @Query("apikey") apikey: String,
        @Query("language") language: String,
        @Query("details") details: Boolean,
        @Query("metric") metric: Boolean,
    ): Observable<List<AccuForecastHourlyResult>>

    @GET("alerts/v1/{city_key}")
    fun getAlertsByCityKey(
        @Path("city_key") cityKey: String,
        @Query("apikey") apikey: String,
        @Query("language") language: String,
        @Query("details") details: Boolean,
    ): Observable<List<AccuAlertResult>>
}
