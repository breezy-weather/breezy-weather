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

package org.breezyweather.sources.ims

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.ims.json.ImsLocationResult
import org.breezyweather.sources.ims.json.ImsWeatherResult
import retrofit2.http.GET
import retrofit2.http.Path

interface ImsApi {

    @GET("{lang}/locations_info")
    fun getLocations(
        @Path("lang") lang: String, // Allowed values: "en", "he" or "ar"
    ): Observable<ImsLocationResult>

    @GET("{lang}/city_portal/{locationId}")
    fun getWeather(
        @Path("lang") lang: String, // Allowed values: "en", "he" or "ar"
        @Path("locationId") locationId: String,
    ): Observable<ImsWeatherResult>
}
