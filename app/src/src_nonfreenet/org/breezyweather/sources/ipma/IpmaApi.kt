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

package org.breezyweather.sources.ipma

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.ipma.json.IpmaAlertResult
import org.breezyweather.sources.ipma.json.IpmaDistrictResult
import org.breezyweather.sources.ipma.json.IpmaForecastResult
import org.breezyweather.sources.ipma.json.IpmaLocationResult
import retrofit2.http.GET
import retrofit2.http.Path

interface IpmaApi {
    @GET("public-data/districts.json")
    fun getDistricts(): Observable<List<IpmaDistrictResult>>

    @GET("public-data/forecast/locations.json")
    fun getLocations(): Observable<List<IpmaLocationResult>>

    @GET("public-data/forecast/aggregate/{globalIdLocal}.json")
    fun getForecast(
        @Path("globalIdLocal") globalIdLocal: String,
    ): Observable<List<IpmaForecastResult>>

    @GET("public-data/warnings/warnings_www.json")
    fun getAlerts(): Observable<IpmaAlertResult>
}
