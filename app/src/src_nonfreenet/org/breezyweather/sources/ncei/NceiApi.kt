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

package org.breezyweather.sources.ncei

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.ncei.json.NceiDataResult
import org.breezyweather.sources.ncei.json.NceiStationsResult
import retrofit2.http.GET
import retrofit2.http.Query

interface NceiApi {
    @GET("access/services/search/v1/data")
    fun getStations(
        @Query("dataTypes") dataTypes: String = "TMAX,TMIN",
        @Query("bbox") bbox: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("dataset") dataset: String = "global-summary-of-the-month",
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0,
    ): Observable<NceiStationsResult>

    @GET("access/services/data/v1")
    fun getData(
        @Query("dataset") dataset: String = "global-summary-of-the-month",
        @Query("dataTypes") dataTypes: String = "TMAX,TMIN",
        @Query("stations") stations: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("includeAttributes") includeAttributes: Boolean = true,
        @Query("includeStationName") includeStationName: Boolean = true,
        @Query("includeStationLocation") includeStationLocation: Boolean = true,
        @Query("units") units: String = "metric",
        @Query("format") format: String = "json",
    ): Observable<List<NceiDataResult>>
}
