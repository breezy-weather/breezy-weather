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
import org.breezyweather.common.basic.models.options.index.AllergenIndex
import java.io.Serializable

/**
 * Pollen (tree, grass, weed) and mold
 */
class Allergen(
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
    val mold: Int? = null
) : Serializable {

    val validAllergens: List<AllergenIndex>
        get() {
            return listOf(
                AllergenIndex.TREE,
                AllergenIndex.ALDER,
                AllergenIndex.BIRCH,
                AllergenIndex.GRASS,
                AllergenIndex.OLIVE,
                AllergenIndex.RAGWEED,
                AllergenIndex.MUGWORT,
                AllergenIndex.MOLD
            ).filter { getConcentration(it) != null }
        }

    fun getIndex(allergen: AllergenIndex? = null): Int? {
        return if (allergen == null) { // Global allergen index
            val allergensIndex: List<Int> = listOfNotNull(
                getIndex(AllergenIndex.TREE),
                getIndex(AllergenIndex.ALDER),
                getIndex(AllergenIndex.BIRCH),
                getIndex(AllergenIndex.GRASS),
                getIndex(AllergenIndex.OLIVE),
                getIndex(AllergenIndex.RAGWEED),
                getIndex(AllergenIndex.MUGWORT),
                getIndex(AllergenIndex.MOLD)
            )
            if (allergensIndex.isNotEmpty()) allergensIndex.max() else null
        } else { // Specific allergen
            allergen.getIndex(getConcentration(allergen)?.toDouble())
        }
    }

    fun getConcentration(allergen: AllergenIndex) = when (allergen) {
        AllergenIndex.TREE -> tree
        AllergenIndex.ALDER -> alder
        AllergenIndex.BIRCH -> birch
        AllergenIndex.GRASS -> grass
        AllergenIndex.OLIVE -> olive
        AllergenIndex.RAGWEED -> ragweed
        AllergenIndex.MUGWORT -> mugwort
        AllergenIndex.MOLD -> mold
    }

    fun getIndexName(context: Context, allergen: AllergenIndex? = null): String? {
        return if (allergen == null) { // Global allergen risk
            AllergenIndex.getAllergenIndexToName(context, getIndex())
        } else { // Specific allergen
            allergen.getName(context, getConcentration(allergen)?.toDouble())
        }
    }

    fun getName(context: Context, allergen: AllergenIndex): String {
        return context.getString(allergen.allergenName)
    }

    @ColorInt
    fun getColor(context: Context, allergen: AllergenIndex? = null): Int {
        return if (allergen == null) {
            AllergenIndex.getAllergenIndexToColor(context, getIndex())
        } else { // Specific allergen
            allergen.getColor(context, getConcentration(allergen)?.toDouble())
        }
    }

    val isIndexValid: Boolean
        get() = getIndex() != null

    val isValid: Boolean
        get() = tree != null || alder != null || birch != null || grass != null
            || olive != null || ragweed != null || mugwort != null || mold != null
}
