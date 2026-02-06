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

/**
 * Interface for sources providing pollen data expressed in a scale or level way
 * (for example 0 to 5)
 */
interface PollenIndexSource : Source {

    /**
     * Array containing 0 to max level non-translatable labels
     * If a data exceed max level, it will fallback to last item
     */
    val pollenLabels: Int

    /**
     * Array containing 0 to max level colors
     * If a data exceed max level, it will fallback to last item
     */
    val pollenColors: Int
}
