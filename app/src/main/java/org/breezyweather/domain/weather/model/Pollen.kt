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

package org.breezyweather.domain.weather.model

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import breezyweather.domain.weather.model.Pollen
import org.breezyweather.R
import org.breezyweather.common.source.PollenIndexSource
import org.breezyweather.domain.weather.index.PollenIndex

val Pollen.validPollens: List<PollenIndex>
    get() {
        return PollenIndex.entries.filter { getConcentration(it) != null }
    }

val Pollen.pollensWithConcentration: List<PollenIndex>
    get() {
        return PollenIndex.entries.filter { pollenIndex ->
            val concentration = getConcentration(pollenIndex)
            concentration != null && concentration > 0
        }
    }

fun Pollen.getIndex(pollen: PollenIndex? = null): Int? {
    return if (pollen == null) { // Global pollen index
        val pollensIndex: List<Int> = PollenIndex.entries.mapNotNull { getIndex(it) }
        if (pollensIndex.isNotEmpty()) pollensIndex.max() else null
    } else { // Specific pollen
        pollen.getIndex(getConcentration(pollen)?.toDouble())
    }
}

fun Pollen.getPollenWithMaxIndex(): PollenIndex? {
    val pollensIndex: Map<PollenIndex, Int> = PollenIndex.entries
        .filter { it != PollenIndex.MOLD }
        .mapNotNull { pollenIndex ->
            getIndex(pollenIndex)?.let {
                if (it > 0) pollenIndex to it else null
            }
        }.toMap()
    return if (pollensIndex.isNotEmpty()) {
        pollensIndex.maxBy { it.value }.key
    } else {
        null
    }
}

fun Pollen.getConcentration(pollen: PollenIndex) = when (pollen) {
    PollenIndex.ALDER -> alder
    PollenIndex.ASH -> ash
    PollenIndex.BIRCH -> birch
    PollenIndex.CHESTNUT -> chestnut
    PollenIndex.CYPRESS -> cypress
    PollenIndex.GRASS -> grass
    PollenIndex.HAZEL -> hazel
    PollenIndex.HORNBEAM -> hornbeam
    PollenIndex.LINDEN -> linden
    PollenIndex.MOLD -> mold
    PollenIndex.MUGWORT -> mugwort
    PollenIndex.OAK -> oak
    PollenIndex.OLIVE -> olive
    PollenIndex.PLANE -> plane
    PollenIndex.PLANTAIN -> plantain
    PollenIndex.POPLAR -> poplar
    PollenIndex.RAGWEED -> ragweed
    PollenIndex.SORREL -> sorrel
    PollenIndex.TREE -> tree
    PollenIndex.URTICACEAE -> urticaceae
    PollenIndex.WILLOW -> willow
}

fun Pollen.getIndexName(
    context: Context,
    pollen: PollenIndex? = null,
    source: PollenIndexSource? = null,
): String? {
    return if (source != null) {
        if (pollen != null) {
            getConcentration(pollen)?.let {
                context.resources.getStringArray(source.pollenLabels).getOrElse(it) { null }
            }
        } else {
            null
        }
    } else {
        if (pollen == null) { // Global pollen risk
            PollenIndex.getPollenIndexToName(context, getIndex())
        } else { // Specific pollen
            pollen.getName(context, getConcentration(pollen)?.toDouble())
        }
    }
}

fun Pollen.getSummary(
    context: Context,
    source: PollenIndexSource? = null,
): String {
    return pollensWithConcentration.joinToString(context.getString(R.string.comma_separator)) {
        getName(context, it) +
            context.getString(R.string.colon_separator) +
            getIndexName(context, it, source)
    }
}

fun Pollen.getName(context: Context, pollen: PollenIndex): String {
    return context.getString(pollen.pollenName)
}

@ColorInt
fun Pollen.getColor(
    context: Context,
    pollen: PollenIndex? = null,
    source: PollenIndexSource? = null,
): Int {
    return if (source != null) {
        if (pollen != null) {
            getConcentration(pollen)?.let {
                context.resources.getIntArray(source.pollenColors).getOrElse(it) { Color.TRANSPARENT }
            } ?: Color.TRANSPARENT
        } else {
            Color.TRANSPARENT
        }
    } else {
        if (pollen == null) {
            PollenIndex.getPollenIndexToColor(context, getIndex())
        } else { // Specific pollen
            pollen.getColor(context, getConcentration(pollen)?.toDouble())
        }
    }
}

val Pollen.isIndexValid: Boolean
    get() = getIndex() != null
