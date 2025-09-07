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

package org.breezyweather.unit.computing

import org.breezyweather.unit.precipitation.Precipitation
import org.breezyweather.unit.precipitation.Precipitation.Companion.micrometers
import org.breezyweather.unit.ratio.Ratio
import org.breezyweather.unit.ratio.Ratio.Companion.permille
import org.breezyweather.unit.temperature.Temperature
import kotlin.math.roundToLong

/**
 * Compute total precipitation in liquid equivalent.
 * Uses Cobb-Waldstreicher method (2011 version) for snow.
 * (X inches of snow = 1 inch of water equivalent, varies with temperature)
 * Uses 0.92 as ratio for ice equivalent:
 *  - density of ice at 0°C = 916.2 kg/m³
 *  - density of water at 0°C = 999.8 kg/m³
 * Ice density increases insignificantly as temperature drops further.
 * 0.92 is a good enough approximation without adding unnecessary computation.
 * Source: https://www.engineeringtoolbox.com/ice-thermal-properties-d_576.html
 */
fun computeTotalPrecipitation(
    temperature: Temperature?,
    rain: Precipitation?,
    snow: Precipitation?,
    ice: Precipitation?,
): Precipitation? {
    if (rain == null && snow == null && ice == null) return null
    val snowEquivalent = snow?.value?.div(computeSnowToLiquidRatio(temperature))?.roundToLong()
    val iceEquivalent = ice?.value?.times(0.92)?.roundToLong()
    return (rain?.value ?: 0).plus(snowEquivalent ?: 0).plus(iceEquivalent ?: 0).micrometers
}

fun computeTotalProbabilityOfPrecipitation(
    thunderstorm: Ratio?,
    rain: Ratio?,
    snow: Ratio?,
    ice: Ratio?,
): Ratio? {
    if (thunderstorm == null && rain == null && snow == null && ice == null) return null
    return maxOf(thunderstorm?.value ?: 0, rain?.value ?: 0, snow?.value ?: 0, ice?.value ?: 0).permille
}

private fun computeSnowToLiquidRatio(
    temperature: Temperature?,
): Double {
    if (temperature == null) {
        return 7.2
    }
    SNOW_TO_LIQUID_RATIOS.forEachIndexed { i, ratio ->
        if (i >= SNOW_TO_LIQUID_RATIOS.size - 1) { // > 300°C
            return ratio.second
        }
        val next = SNOW_TO_LIQUID_RATIOS[i + 1]
        if (temperature.inCelsius >= ratio.first && temperature.inCelsius < next.first) {
            return ratio.second +
                (next.second - ratio.second) / (next.first - ratio.first) *
                (temperature.inCelsius - ratio.first)
        }
    }
    // Theoretically we should never reach this.
    return 1.0
}

/**
 * Snow to liquid ratios based on the 2011 version of the
 * Cobb-Waldstreicher method. Ratio is set to 1.0 for
 * temperatures above freezing.
 * Source: https://www.weather.gov/media/mdl/SLR.pdf
 */
private val SNOW_TO_LIQUID_RATIOS = listOf(
    Pair(-300.0, 7.2),
    Pair(-30.0, 7.2),
    Pair(-28.0, 6.8),
    Pair(-26.0, 7.0),
    Pair(-24.0, 8.8),
    Pair(-22.0, 12.0),
    Pair(-20.0, 18.0),
    Pair(-18.0, 23.0),
    Pair(-16.0, 26.0),
    Pair(-14.0, 22.5),
    Pair(-12.0, 17.5),
    Pair(-10.0, 12.0),
    Pair(-8.0, 9.5),
    Pair(-6.0, 9.0),
    Pair(-4.0, 8.5),
    Pair(-2.0, 7.0),
    Pair(0.0, 3.0),
    Pair(1.0, 1.0),
    Pair(300.0, 1.0)
)
