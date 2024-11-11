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

package org.breezyweather.sources.mgm

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.mgm.json.MgmAlertResult
import org.breezyweather.sources.mgm.json.MgmCurrentResult
import org.breezyweather.sources.mgm.json.MgmDailyForecastResult
import org.breezyweather.sources.mgm.json.MgmHourlyForecastResult
import org.breezyweather.sources.mgm.json.MgmLocationResult
import org.breezyweather.sources.mgm.json.MgmNormalsResult
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface MgmApi {
    @Headers("Origin: $ORIGIN_URL")
    @GET("web/merkezler/lokasyon")
    fun getLocation(
        @Query("enlem") lat: Double,
        @Query("boylam") lon: Double
    ): Observable<MgmLocationResult>

    @Headers("Origin: $ORIGIN_URL")
    @GET("web/tahminler/saatlik")
    fun getHourly(
        @Query("istno") station: String
    ): Observable<List<MgmHourlyForecastResult>>

    @Headers("Origin: $ORIGIN_URL")
    @GET("web/sondurumlar")
    fun getCurrent(
        @Query("merkezid") station: String
    ): Observable<List<MgmCurrentResult>>

    @Headers("Origin: $ORIGIN_URL")
    @GET("web/tahminler/gunluk")
    fun getDaily(
        @Query("istno") station: String
    ): Observable<List<MgmDailyForecastResult>>

    @Headers("Origin: $ORIGIN_URL")
    @GET("web/meteoalarm/{day}")
    fun getAlert(
        @Path("day") day: String
    ): Observable<List<MgmAlertResult>>

    @Headers("Origin: $ORIGIN_URL")
    @GET("web/ucdegerler")
    fun getNormals(
        @Query("merkezid") station: String,
        @Query("ay") month: Int,
        @Query("gun") day: Int
    ): Observable<List<MgmNormalsResult>>

    companion object {
        const val ORIGIN_URL = "https://www.mgm.gov.tr"
    }

}
