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

package org.breezyweather.sources.metie

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.metie.json.MetIeLocationResult
import org.breezyweather.sources.metie.json.MetIeWarningResult
import retrofit2.http.GET
import retrofit2.http.Path

interface MetIeJsonApi {

    @GET("v3/warnings")
    fun getWarnings(): Observable<MetIeWarningResult>

    @GET("location/reverse/{lat}/{lon}")
    fun getReverseLocation(
        @Path("lat") lat: Double,
        @Path("lon") lon: Double,
    ): Observable<MetIeLocationResult>
}
