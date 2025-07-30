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

import androidx.annotation.StringRes
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import org.breezyweather.R
import org.breezyweather.domain.source.resourceName

interface Source {
    /**
     * Id for the source. Must be unique.
     */
    val id: String

    /**
     * Name of the source.
     */
    val name: String

    /**
     * How this source should be grouped:
     * - Recommended
     * - Worldwide
     * - Continent
     */
    @StringRes
    fun getGroup(location: Location, feature: SourceFeature): Int {
        return if (this is FeatureSource &&
            getFeaturePriorityForLocation(location, feature) >= 0
        ) {
            R.string.weather_source_recommended
        } else if (this is HttpSource) {
            continent.resourceName
        } else {
            SourceContinent.WORLDWIDE.resourceName
        }
    }
}
