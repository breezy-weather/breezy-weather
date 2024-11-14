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

package org.breezyweather.domain.weather.index

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import org.breezyweather.R
import kotlin.math.roundToInt

enum class PollenIndex(
    val id: String,
    @StringRes val pollenName: Int,
    val thresholds: List<Int>,
) {
    ALDER("alder", R.string.pollen_alnus, listOf(0, 10, 50, 100, 300)),
    ASH("ash", R.string.pollen_fraxinus, listOf(0, 30, 100, 200, 400)),
    BIRCH("birch", R.string.pollen_betula, listOf(0, 10, 50, 100, 300)),
    CHESTNUT("chestnut", R.string.pollen_castanea, listOf(0, 1, 2, 3, 4)), // TODO
    // COTTONWOOD("cottonwood", R.string.pollen_cottonwood, listOf(0, 50, 200, 400, 800)),
    CYPRESS("cypress", R.string.pollen_cupressaceae_taxaceae, listOf(0, 1, 2, 3, 4)), // TODO
    // ELM("elm", R.string.pollen_elm, listOf(0, 30, 50, 100, 200)),
    GRASS("grass", R.string.pollen_poaeceae, listOf(0, 5, 25, 50, 100)),
    HAZEL("hazel", R.string.pollen_corylus, listOf(0, 1, 2, 3, 4)), // TODO
    HORNBEAM("hornbeam", R.string.pollen_carpinus, listOf(0, 1, 2, 3, 4)), // TODO
    // JAPANESE_CYPRESS("cypress", R.string.pollen_japanese_cypress, listOf(0, 3, 11, 19, 39)),
    // JUNIPER("juniper", R.string.pollen_juniper, listOf(0, 10, 50, 140, 280)),
    LINDEN("linden", R.string.pollen_tilia, listOf(0, 1, 2, 3, 4)), // TODO
    // MAPLE("maple", R.string.pollen_maple, listOf(0, 30, 50, 100, 200)),
    MOLD("mold", R.string.pollen_mold, listOf(0, 6500, 13000, 50000, 65000)),
    MUGWORT("mugwort", R.string.pollen_artemisia, listOf(0, 5, 11, 23, 50)), // TODO: To be checked
    OAK("oak", R.string.pollen_quercus, listOf(0, 50, 100, 200, 400)),
    OLIVE("olive", R.string.pollen_olea, listOf(0, 10, 50, 200, 400)),
    // PINE("pine", R.string.pollen_platanus, listOf(0, 50, 200, 500, 1000)),
    PLANE("plane", R.string.pollen_platanus, listOf(0, 1, 2, 3, 4)), // TODO
    PLANTAIN("plantain", R.string.pollen_plantaginaceae, listOf(0, 1, 2, 3, 4)), // TODO
    POPLAR("poplar", R.string.pollen_populus, listOf(0, 1, 2, 3, 4)), // TODO
    RAGWEED("ragweed", R.string.pollen_ambrosia, listOf(0, 5, 11, 23, 50)),
    SORREL("sorrel", R.string.pollen_rumex, listOf(0, 1, 2, 3, 4)), // TODO
    TREE("tree", R.string.pollen_tree, listOf(0, 10, 50, 100, 300)),
    URTICACEAE("urticaceae", R.string.pollen_urticaceae, listOf(0, 1, 2, 3, 4)), // TODO
    WILLOW("willow", R.string.pollen_salix, listOf(0, 1, 2, 3, 4)),
    ; // TODO

    companion object {
        // No index exists, but let’s make a fake one to help with graphics
        val pollenIndexThresholds = listOf(0, 25, 50, 75, 100)
        val namesArrayId = R.array.pollen_levels
        val colorsArrayId = R.array.pollen_level_colors

        fun getPollenIndexToLevel(pollenIndex: Int?): Int? {
            if (pollenIndex == null) return null
            val level = pollenIndexThresholds.indexOfLast { pollenIndex >= it }
            return if (level >= 0) level else null
        }

        @ColorInt
        fun getPollenIndexToColor(context: Context, pollenIndex: Int?): Int {
            if (pollenIndex == null) return Color.TRANSPARENT
            if (pollenIndex == 0) return ContextCompat.getColor(context, R.color.pollenLevel_0)
            val level = getPollenIndexToLevel(pollenIndex)
            return if (level != null) {
                context.resources.getIntArray(colorsArrayId).getOrNull(level) ?: Color.TRANSPARENT
            } else {
                Color.TRANSPARENT
            }
        }

        fun getPollenIndexToName(context: Context, pollenIndex: Int?): String? {
            if (pollenIndex == null) return null
            if (pollenIndex == 0) return context.getString(R.string.pollen_level_0)
            val level = getPollenIndexToLevel(pollenIndex)
            return if (level != null) context.resources.getStringArray(namesArrayId).getOrNull(level) else null
        }
    }

    private fun getIndex(cp: Double, bpLo: Int, bpHi: Int, inLo: Int, inHi: Int): Int {
        // Result will be incorrect if we don’t cast to double
        return (
            (inHi.toDouble() - inLo.toDouble()) / (bpHi.toDouble() - bpLo.toDouble()) * (cp - bpLo.toDouble()) +
                inLo.toDouble()
            ).roundToInt()
    }

    private fun getIndex(cp: Double, level: Int): Int {
        return if (level < thresholds.lastIndex) {
            getIndex(
                cp,
                thresholds[level],
                thresholds[level + 1],
                pollenIndexThresholds[level],
                pollenIndexThresholds[level + 1]
            )
        } else {
            // Continue producing a linear index above lastIndex
            ((cp * pollenIndexThresholds.last()) / thresholds.last()).roundToInt()
        }
    }

    fun getIndex(cp: Double?): Int? {
        if (cp == null) return null
        val level = thresholds.indexOfLast { cp >= it }
        return if (level >= 0) getIndex(cp, level) else null
    }

    fun getLevel(cp: Double?): Int? {
        if (cp == null) return null
        val level = thresholds.indexOfLast { cp >= it }
        return if (level >= 0) level else null
    }

    fun getName(context: Context, cp: Double?): String? = getPollenIndexToName(context, getIndex(cp))

    @ColorInt
    fun getColor(context: Context, cp: Double?): Int = getPollenIndexToColor(context, getIndex(cp))
}
