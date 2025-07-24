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

package org.breezyweather.sources.geosphereat

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.geosphereat.json.GeoSphereAtTimeseriesResult
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GeoSphereAtApi {

    @GET("v1/timeseries/forecast/nwp-v1-1h-2500m")
    fun getHourlyForecast(
        @Query("lat_lon", encoded = true) latLon: String,
        @Query("parameters", encoded = true) parameters: String,
    ): Observable<GeoSphereAtTimeseriesResult>

    @GET("v1/timeseries/forecast/nowcast-v1-15min-1km")
    fun getNowcast(
        @Query("lat_lon", encoded = true) latLon: String,
        @Query("parameters", encoded = true) parameters: String,
    ): Observable<GeoSphereAtTimeseriesResult>

    @GET("v1/timeseries/forecast/chem-v2-1h-{km}km")
    fun getAirQuality(
        @Path("km") km: Int, // 3 or 9 depending on bbox
        @Query("lat_lon", encoded = true) latLon: String,
        @Query("parameters", encoded = true) parameters: String,
    ): Observable<GeoSphereAtTimeseriesResult>
}
