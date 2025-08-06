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
import androidx.annotation.ColorInt
import breezyweather.domain.weather.model.AirQuality
import org.breezyweather.domain.weather.index.PollutantIndex

val AirQuality.validPollutants: List<PollutantIndex>
    get() {
        return listOf(
            PollutantIndex.NO2,
            PollutantIndex.O3,
            PollutantIndex.PM10,
            PollutantIndex.PM25,
            PollutantIndex.SO2,
            PollutantIndex.CO
        ).filter { getConcentration(it) != null }
    }

fun AirQuality.getIndex(pollutant: PollutantIndex? = null): Int? {
    return if (pollutant == null) { // Air Quality
        val pollutantsAqi: List<Int> = listOfNotNull(
            getIndex(PollutantIndex.O3),
            getIndex(PollutantIndex.NO2),
            getIndex(PollutantIndex.PM10),
            getIndex(PollutantIndex.PM25)
        )
        if (pollutantsAqi.isNotEmpty()) pollutantsAqi.max() else null
    } else { // Specific pollutant
        pollutant.getIndex(getConcentration(pollutant))
    }
}

fun AirQuality.getConcentration(pollutant: PollutantIndex) = when (pollutant) {
    PollutantIndex.PM25 -> pM25
    PollutantIndex.PM10 -> pM10
    PollutantIndex.O3 -> o3
    PollutantIndex.NO2 -> nO2
    PollutantIndex.SO2 -> sO2
    PollutantIndex.CO -> cO
}

fun AirQuality.getName(context: Context, pollutant: PollutantIndex? = null): String? {
    return if (pollutant == null) { // Air Quality
        PollutantIndex.getAqiToName(context, getIndex())
    } else { // Specific pollutant
        pollutant.getName(context, getConcentration(pollutant))
    }
}

fun AirQuality.getDescription(context: Context, pollutant: PollutantIndex? = null): String? {
    return if (pollutant == null) { // Air Quality
        PollutantIndex.getAqiToDescription(context, getIndex())
    } else { // Specific pollutant
        pollutant.getDescription(context, getConcentration(pollutant))
    }
}

@ColorInt
fun AirQuality.getColor(context: Context, pollutant: PollutantIndex? = null): Int {
    return if (pollutant == null) {
        PollutantIndex.getAqiToColor(context, getIndex())
    } else { // Specific pollutant
        pollutant.getColor(context, getConcentration(pollutant))
    }
}
