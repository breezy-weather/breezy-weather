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
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceFeature
import io.reactivex.rxjava3.core.Observable

/**
 * Implement this if you need parameters such as an ID for the location
 * For example, before fetching weather, you need to call an URL with longitude,latitude that
 * will then give you the ID that needs to be stored
 * ONLY used before fetching main weather OR secondary weather data
 */
interface LocationParametersSource : Source {

    /**
     * Parameters:
     * - the location
     * - if coordinates were changed (only on the current location)
     * - list of features requested. Empty if not specific to a feature (main source)
     */
    fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature> = emptyList(),
    ): Boolean

    /**
     * Fetch any parameters you need and then make a map. For example :
     * {"gridId": "20", "gridX": "30", "gridY": "25"}
     */
    fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>>
}
