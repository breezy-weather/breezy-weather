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

package breezyweather.domain.weather.model

import java.io.Serializable

/**
 * Pollen (tree, grass, weed) and mold
 */
class Pollen(
    val alder: Int? = null,
    val ash: Int? = null,
    val birch: Int? = null,
    val chestnut: Int? = null,
    val cypress: Int? = null,
    val grass: Int? = null,
    val hazel: Int? = null,
    val hornbeam: Int? = null,
    val linden: Int? = null,
    val mold: Int? = null, // Not a pollen, but probably relevant to put with them
    val mugwort: Int? = null,
    val oak: Int? = null,
    val olive: Int? = null,
    val plane: Int? = null,
    val plantain: Int? = null,
    val poplar: Int? = null,
    val ragweed: Int? = null,
    val sorrel: Int? = null,
    val tree: Int? = null,
    val urticaceae: Int? = null,
    val willow: Int? = null,
) : Serializable {

    val isMoldValid: Boolean
        get() = mold != null

    val isValid: Boolean
        get() = alder != null ||
            ash != null ||
            birch != null ||
            chestnut != null ||
            cypress != null ||
            grass != null ||
            hazel != null ||
            hornbeam != null ||
            linden != null ||
            mold != null ||
            mugwort != null ||
            oak != null ||
            olive != null ||
            plane != null ||
            plantain != null ||
            poplar != null ||
            ragweed != null ||
            sorrel != null ||
            tree != null ||
            urticaceae != null ||
            willow != null
}
