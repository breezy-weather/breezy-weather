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

package org.breezyweather.sources.namem

import io.reactivex.rxjava3.core.Observable
import okhttp3.RequestBody
import org.breezyweather.sources.namem.json.NamemAirQualityResult
import org.breezyweather.sources.namem.json.NamemCurrentResult
import org.breezyweather.sources.namem.json.NamemDailyResult
import org.breezyweather.sources.namem.json.NamemHourlyResult
import org.breezyweather.sources.namem.json.NamemNormalsResult
import org.breezyweather.sources.namem.json.NamemStationsResult
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface NamemApi {
    @GET("api/get/obs/aimags")
    fun getStations(): Observable<NamemStationsResult>

    @Headers("Origin: $ORIGIN_URL")
    @POST("api/get/obs/aws")
    fun getCurrent(
        @Body body: RequestBody,
    ): Observable<NamemCurrentResult>

    @Headers("Origin: $ORIGIN_URL")
    @POST("api/get/forecast/fore3hours")
    fun getHourly(
        @Body body: RequestBody,
    ): Observable<NamemHourlyResult>

    @Headers("Origin: $ORIGIN_URL")
    @POST("api/get/forecast/5day")
    fun getDaily(
        @Body body: RequestBody,
    ): Observable<NamemDailyResult>

    @Headers("Origin: $ORIGIN_URL")
    @POST("api/get/forecast/monthly")
    fun getNormals(
        @Body body: RequestBody,
    ): Observable<NamemNormalsResult>

    @Headers("Origin: $ORIGIN_URL")
    @POST("api/get/agaar/multi")
    fun getAirQuality(): Observable<NamemAirQualityResult>

    companion object {
        const val ORIGIN_URL = "https://www.weather.gov.mn"
    }
}
