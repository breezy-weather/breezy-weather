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

package org.breezyweather.sources.lhmt

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.lhmt.json.LhmtLocationsResult
import org.breezyweather.sources.lhmt.json.LhmtWeatherResult
import retrofit2.http.GET
import retrofit2.http.Path

interface LhmtApi {
    @GET("v1/places")
    fun getForecastLocations(): Observable<List<LhmtLocationsResult>>

    @GET("v1/stations")
    fun getCurrentLocations(): Observable<List<LhmtLocationsResult>>

    @GET("v1/places/{code}/forecasts/long-term")
    fun getForecast(
        @Path("code") code: String,
    ): Observable<LhmtWeatherResult>

    @GET("v1/stations/{code}/observations/latest")
    fun getCurrent(
        @Path("code") code: String,
    ): Observable<LhmtWeatherResult>
}
