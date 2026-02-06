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

package org.breezyweather.common.source

import androidx.annotation.DrawableRes
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceFeature

/**
 * Source with features
 */
interface FeatureSource : Source {

    /**
     * An optional icon for the attribution page.
     * /!\ Only include it if it is mandatory in the attribution, as we don’t want to bundle copyrighted icons which
     * we don’t have the right to use!
     * Example: return R.drawable.accu_icon
     */
    @DrawableRes
    fun getAttributionIcon(): Int? {
        return null
    }

    /**
     * List the features by the source as keys
     * Values are credits and acknowledgments that will be shown at the bottom of main screen
     * Please check terms of the source to be sure to put the correct term here
     * Example: <SourceFeature.FORECAST, "MyGreatApi (CC BY 4.0)">
     */
    val supportedFeatures: Map<SourceFeature, String>

    /**
     * May be used when you don't have reverse geocoding implemented and you want to filter
     * location results from default location search source to only include some countries
     * for example
     */
    fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean = true

    /**
     * Used to identify recommended sources by countries, ordered by priority descending.
     * Any positive number can be used here, but we recommend using the available constants
     *
     * For example, worldwide sources will usually return PRIORITY_NONE (not recommended)
     * National sources should return PRIORITY_HIGHEST here, unless there are multiple national sources
     * and one is better than the other. In that case, sort them using the available preset priorities
     */
    fun getFeaturePriorityForLocation(
        location: Location,
        feature: SourceFeature,
    ): Int = PRIORITY_NONE

    companion object {
        const val PRIORITY_HIGHEST = 100
        const val PRIORITY_HIGH = 75
        const val PRIORITY_MEDIUM = 50
        const val PRIORITY_LOW = 25
        const val PRIORITY_LOWEST = 0
        const val PRIORITY_NONE = -1
    }
}
