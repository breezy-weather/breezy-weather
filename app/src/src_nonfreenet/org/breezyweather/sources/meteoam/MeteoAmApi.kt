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

package org.breezyweather.sources.meteoam

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.meteoam.json.MeteoAmForecastResult
import org.breezyweather.sources.meteoam.json.MeteoAmObservationResult
import org.breezyweather.sources.meteoam.json.MeteoAmReverseLocationResult
import retrofit2.http.GET
import retrofit2.http.Path

interface MeteoAmApi {
    @GET("deda-meteograms/api/GetMeteogram/preset1/{lat},{lon}")
    fun getForecast(
        @Path("lat") lat: Double,
        @Path("lon") lon: Double,
    ): Observable<MeteoAmForecastResult>

    @GET("deda-ows/api/GetStationRadius/{lat}/{lon}")
    fun getCurrent(
        @Path("lat") lat: Double,
        @Path("lon") lon: Double,
    ): Observable<MeteoAmObservationResult>

    @GET("geocoder/r/{lon}/{lat}")
    fun getReverseLocation(
        @Path("lat") lat: Double,
        @Path("lon") lon: Double,
    ): Observable<MeteoAmReverseLocationResult>
}
