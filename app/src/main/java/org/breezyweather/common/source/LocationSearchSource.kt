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

package org.breezyweather.common.source

import android.content.Context
import breezyweather.domain.location.model.LocationAddressInfo
import io.reactivex.rxjava3.core.Observable

/**
 * Location search source
 */
interface LocationSearchSource : Source {
    /**
     * Credits and acknowledgments that will be shown at the bottom of main screen
     * Please check terms of the source to be sure to put the correct term here
     * Example: MyGreatApi (CC BY 4.0)
     *
     * Will not be displayed if identical to weatherAttribution
     */
    val locationSearchAttribution: String

    /**
     * Returns a list of Breezy Weather Location results from a query
     */
    fun requestLocationSearch(context: Context, query: String): Observable<List<LocationAddressInfo>>
}
