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
    val thresholds: List<Int>
) {
    TREE("tree", R.string.pollen_tree, listOf(0, 10, 50, 100, 300)),
    ALDER("alder", R.string.pollen_alder, listOf(0, 10, 50, 100, 300)),
    BIRCH("birch", R.string.pollen_birch, listOf(0, 10, 50, 100, 300)),
    GRASS("grass", R.string.pollen_grass, listOf(0, 5, 25, 50, 100)),
    OLIVE("olive", R.string.pollen_olive, listOf(0, 10, 50, 200, 400)),
    RAGWEED("ragweed", R.string.pollen_ragweed, listOf(0, 5, 11, 23, 50)),
    MUGWORT("mugwort", R.string.pollen_mugwort, listOf(0, 5, 11, 23, 50)), // TODO: To be checked
    MOLD("mold", R.string.pollen_mold, listOf(0, 6500, 13000, 50000, 65000, 1000000));

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
            return if (level != null) context.resources.getIntArray(colorsArrayId)
                .getOrNull(level) ?: Color.TRANSPARENT
            else Color.TRANSPARENT
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
        return ((inHi.toDouble() - inLo.toDouble()) / (bpHi.toDouble() - bpLo.toDouble()) * (cp - bpLo.toDouble()) + inLo.toDouble()).roundToInt()
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