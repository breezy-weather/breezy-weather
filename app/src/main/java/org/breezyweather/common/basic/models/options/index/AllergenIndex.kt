package org.breezyweather.common.basic.models.options.index

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import org.breezyweather.R
import kotlin.math.roundToInt

enum class AllergenIndex(
    val id: String,
    @StringRes val allergenName: Int,
    val thresholds: List<Int>
) {
    TREE("tree", R.string.allergen_tree, listOf(0, 10, 50, 100, 300)),
    ALDER("alder", R.string.allergen_alder, listOf(0, 10, 50, 100, 300)),
    BIRCH("birch", R.string.allergen_birch, listOf(0, 10, 50, 100, 300)),
    GRASS("grass", R.string.allergen_grass, listOf(0, 5, 25, 50, 100)),
    OLIVE("olive", R.string.allergen_olive, listOf(0, 10, 50, 200, 400)),
    RAGWEED("ragweed", R.string.allergen_ragweed, listOf(0, 5, 11, 23, 50)),
    MUGWORT("mugwort", R.string.allergen_mugwort, listOf(0, 5, 11, 23, 50)), // TODO: To be checked
    MOLD("mold", R.string.allergen_mold, listOf(0, 6500, 13000, 50000, 65000, 1000000));

    companion object {
        // No index exists, but let’s make a fake one to help with graphics
        val allergenIndexThresholds = listOf(0, 25, 50, 75, 100)
        val namesArrayId = R.array.allergen_levels
        val colorsArrayId = R.array.allergen_level_colors

        fun getAllergenIndexToLevel(allergenIndex: Int?): Int? {
            if (allergenIndex == null) return null
            val level = allergenIndexThresholds.indexOfLast { allergenIndex >= it }
            return if (level >= 0) level else null
        }

        @ColorInt
        fun getAllergenIndexToColor(context: Context, allergenIndex: Int?): Int {
            if (allergenIndex == null) return Color.TRANSPARENT
            if (allergenIndex == 0) return ContextCompat.getColor(context, R.color.allergenLevel_0)
            val level = getAllergenIndexToLevel(allergenIndex)
            return if (level != null) context.resources.getIntArray(colorsArrayId)
                .getOrNull(level) ?: Color.TRANSPARENT
            else Color.TRANSPARENT
        }

        fun getAllergenIndexToName(context: Context, allergenIndex: Int?): String? {
            if (allergenIndex == null) return null
            if (allergenIndex == 0) return context.getString(R.string.allergen_level_0)
            val level = getAllergenIndexToLevel(allergenIndex)
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
                allergenIndexThresholds[level],
                allergenIndexThresholds[level + 1]
            )
        } else {
            // Continue producing a linear index above lastIndex
            ((cp * allergenIndexThresholds.last()) / thresholds.last()).roundToInt()
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

    fun getName(context: Context, cp: Double?): String? = getAllergenIndexToName(context, getIndex(cp))

    @ColorInt
    fun getColor(context: Context, cp: Double?): Int = getAllergenIndexToColor(context, getIndex(cp))
}