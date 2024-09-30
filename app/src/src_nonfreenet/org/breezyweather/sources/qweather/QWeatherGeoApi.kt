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

package org.breezyweather.sources.qweather

import io.reactivex.rxjava3.core.Observable
import org.breezyweather.sources.qweather.json.QWeatherLocationResult
import retrofit2.http.GET
import retrofit2.http.Query

interface QWeatherGeoApi {
    @GET("city/lookup")
    fun cityLookup(
        @Query("location") location: String,
        @Query("key") key: String,
        @Query("lang") lang: String = "en"
    ): Observable<QWeatherLocationResult>
}
