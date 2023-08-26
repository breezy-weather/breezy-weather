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

package org.breezyweather.common.basic.models.weather

import android.content.Context
import androidx.annotation.ColorInt
import org.breezyweather.common.basic.models.options.index.PollenIndex
import java.io.Serializable

/**
 * Pollen (tree, grass, weed) and mold
 */
class Pollen(
    val tree: Int? = null,
    val alder: Int? = null,
    val birch: Int? = null,
    val grass: Int? = null,
    val olive: Int? = null,
    /**
     * Can also be found under the name ambrosia
     */
    val ragweed: Int? = null,
    val mugwort: Int? = null,
    val mold: Int? = null // Not a pollen, but probably relevant to put with them
) : Serializable {

    val validPollens: List<PollenIndex>
        get() {
            return listOf(
                PollenIndex.TREE,
                PollenIndex.ALDER,
                PollenIndex.BIRCH,
                PollenIndex.GRASS,
                PollenIndex.OLIVE,
                PollenIndex.RAGWEED,
                PollenIndex.MUGWORT,
                PollenIndex.MOLD
            ).filter { getConcentration(it) != null }
        }

    fun getIndex(pollen: PollenIndex? = null): Int? {
        return if (pollen == null) { // Global pollen index
            val pollensIndex: List<Int> = listOfNotNull(
                getIndex(PollenIndex.TREE),
                getIndex(PollenIndex.ALDER),
                getIndex(PollenIndex.BIRCH),
                getIndex(PollenIndex.GRASS),
                getIndex(PollenIndex.OLIVE),
                getIndex(PollenIndex.RAGWEED),
                getIndex(PollenIndex.MUGWORT),
                getIndex(PollenIndex.MOLD)
            )
            if (pollensIndex.isNotEmpty()) pollensIndex.max() else null
        } else { // Specific pollen
            pollen.getIndex(getConcentration(pollen)?.toDouble())
        }
    }

    fun getConcentration(pollen: PollenIndex) = when (pollen) {
        PollenIndex.TREE -> tree
        PollenIndex.ALDER -> alder
        PollenIndex.BIRCH -> birch
        PollenIndex.GRASS -> grass
        PollenIndex.OLIVE -> olive
        PollenIndex.RAGWEED -> ragweed
        PollenIndex.MUGWORT -> mugwort
        PollenIndex.MOLD -> mold
    }

    fun getIndexName(context: Context, pollen: PollenIndex? = null): String? {
        return if (pollen == null) { // Global pollen risk
            PollenIndex.getPollenIndexToName(context, getIndex())
        } else { // Specific pollen
            pollen.getName(context, getConcentration(pollen)?.toDouble())
        }
    }

    fun getName(context: Context, pollen: PollenIndex): String {
        return context.getString(pollen.pollenName)
    }

    @ColorInt
    fun getColor(context: Context, pollen: PollenIndex? = null): Int {
        return if (pollen == null) {
            PollenIndex.getPollenIndexToColor(context, getIndex())
        } else { // Specific pollen
            pollen.getColor(context, getConcentration(pollen)?.toDouble())
        }
    }

    val isIndexValid: Boolean
        get() = getIndex() != null

    val isMoldValid: Boolean
        get() = mold != null

    val isValid: Boolean
        get() = tree != null || alder != null || birch != null || grass != null
            || olive != null || ragweed != null || mugwort != null || mold != null
}
