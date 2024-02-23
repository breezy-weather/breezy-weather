package org.breezyweather.domain.weather.model

import android.content.Context
import androidx.annotation.ColorInt
import breezyweather.domain.weather.model.Pollen
import org.breezyweather.domain.weather.index.PollenIndex

val Pollen.validPollens: List<PollenIndex>
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

fun Pollen.getIndex(pollen: PollenIndex? = null): Int? {
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

fun Pollen.getConcentration(pollen: PollenIndex) = when (pollen) {
    PollenIndex.TREE -> tree
    PollenIndex.ALDER -> alder
    PollenIndex.BIRCH -> birch
    PollenIndex.GRASS -> grass
    PollenIndex.OLIVE -> olive
    PollenIndex.RAGWEED -> ragweed
    PollenIndex.MUGWORT -> mugwort
    PollenIndex.MOLD -> mold
}

fun Pollen.getIndexName(context: Context, pollen: PollenIndex? = null): String? {
    return if (pollen == null) { // Global pollen risk
        PollenIndex.getPollenIndexToName(context, getIndex())
    } else { // Specific pollen
        pollen.getName(context, getConcentration(pollen)?.toDouble())
    }
}

fun Pollen.getName(context: Context, pollen: PollenIndex): String {
    return context.getString(pollen.pollenName)
}

@ColorInt
fun Pollen.getColor(context: Context, pollen: PollenIndex? = null): Int {
    return if (pollen == null) {
        PollenIndex.getPollenIndexToColor(context, getIndex())
    } else { // Specific pollen
        pollen.getColor(context, getConcentration(pollen)?.toDouble())
    }
}

val Pollen.isIndexValid: Boolean
    get() = getIndex() != null