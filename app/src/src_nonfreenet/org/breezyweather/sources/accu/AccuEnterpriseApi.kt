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

package org.breezyweather.sources.accu

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.accu.json.AccuAirQualityResult
import org.breezyweather.sources.accu.json.AccuAlertResult
import org.breezyweather.sources.accu.json.AccuClimoSummaryResult
import org.breezyweather.sources.accu.json.AccuMinutelyResult
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Accu api.
 */
interface AccuEnterpriseApi : AccuDeveloperApi {

    @GET("forecasts/v1/minute/{minutes}minute")
    fun getMinutely(
        @Path("minutes") minutes: Int,
        @Query("apikey") apikey: String,
        @Query("q") q: String,
        @Query("language") language: String,
        @Query("details") details: Boolean,
    ): Observable<AccuMinutelyResult>

    @GET("alerts/v1/geoposition")
    fun getAlertsByPosition(
        @Query("apikey") apikey: String,
        @Query("q") q: String,
        @Query("language") language: String,
        @Query("details") details: Boolean,
    ): Observable<List<AccuAlertResult>>

    @GET("airquality/v2/forecasts/hourly/96hour/{city_key}")
    fun getAirQuality(
        @Path("city_key") cityKey: String,
        @Query("apikey") apikey: String,
        @Query("pollutants") pollutants: Boolean,
        @Query("language") language: String,
    ): Observable<AccuAirQualityResult>

    @GET("climo/v1/summary/{year}/{month}/{city_key}")
    fun getClimoSummary(
        @Path("year") year: Int,
        @Path("month") month: Int,
        @Path("city_key") cityKey: String,
        @Query("apikey") apikey: String,
        @Query("language") language: String,
        @Query("details") details: Boolean,
    ): Observable<AccuClimoSummaryResult>
}
