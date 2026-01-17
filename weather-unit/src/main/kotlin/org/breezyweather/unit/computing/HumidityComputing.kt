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

import org.breezyweather.unit.ratio.Ratio
import org.breezyweather.unit.ratio.Ratio.Companion.fraction
import org.breezyweather.unit.temperature.Temperature
import org.breezyweather.unit.temperature.Temperature.Companion.celsius
import kotlin.math.exp
import kotlin.math.ln

/**
 * Compute relative humidity from temperature and dew point
 * Uses Magnus approximation with Arden Buck best variable set
 * TODO: Unit test
 */
fun computeRelativeHumidity(
    temperature: Temperature?,
    dewPoint: Temperature?,
): Ratio? {
    if (temperature == null || dewPoint == null) return null

    val b = if (temperature < 0.celsius) 17.966 else 17.368
    val c = if (temperature < 0.celsius) 227.15 else 238.88 // °C

    return (
        exp((b * dewPoint.inCelsius).div(c + dewPoint.inCelsius)) /
            exp((b * temperature.inCelsius).div(c + temperature.inCelsius))
        ).fraction
}

/**
 * Compute dew point from temperature and relative humidity
 * Uses Magnus approximation with Arden Buck best variable set
 * TODO: Unit test
 *
 * @param temperature
 * @param relativeHumidity
 */
fun computeDewPoint(
    temperature: Temperature?,
    relativeHumidity: Ratio?,
): Temperature? {
    if (temperature == null || relativeHumidity == null) return null

    val b = if (temperature < 0.celsius) 17.966 else 17.368
    val c = if (temperature < 0.celsius) 227.15 else 238.88 // °C

    val magnus = ln(relativeHumidity.inFraction) + (b * temperature.inCelsius) / (c + temperature.inCelsius)
    return ((c * magnus) / (b - magnus)).celsius
}
