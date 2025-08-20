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

import org.breezyweather.unit.pressure.Pressure
import org.breezyweather.unit.ratio.Ratio
import org.breezyweather.unit.ratio.Ratio.Companion.fraction
import org.breezyweather.unit.speed.Speed
import org.breezyweather.unit.speed.Speed.Companion.kilometersPerHour
import org.breezyweather.unit.temperature.Temperature
import org.breezyweather.unit.temperature.Temperature.Companion.celsius
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow

/**
 * TODO: Use our typed values
 * Compute mean sea level pressure (MSLP) from barometric pressure and altitude.
 * Optional elements can be provided for minor adjustments.
 * Source: https://integritext.net/DrKFS/correctiontosealevel.htm
 *
 * To compute barometric pressure from MSLP,
 * simply enter negative altitude.
 *
 * @param barometricPressure in hPa
 * @param altitude in meters
 * @param temperature in °C (optional)
 * @param humidity in % (optional)
 * @param latitude in ° (optional)
 */
fun computeMeanSeaLevelPressure(
    barometricPressure: Double?,
    altitude: Double?,
    temperature: Double? = null,
    humidity: Double? = null,
    latitude: Double? = null,
): Double? {
    // There is nothing to calculate if barometric pressure or altitude is null.
    if (barometricPressure == null || altitude == null) {
        return null
    }

    // Source: http://www.bom.gov.au/info/thermal_stress/#atapproximation
    val waterVaporPressure = if (humidity != null && temperature != null) {
        humidity / 100 * 6.105 * exp(17.27 * temperature / (237.7 + temperature))
    } else {
        0.0
    }

    // adjustment for temperature
    val term1 = 1.0 + 0.0037 * (temperature ?: 0.0)

    // adjustment for humidity
    val term2 = 1.0 / (1.0 - 0.378 * waterVaporPressure / barometricPressure)

    // adjustment for asphericity of the Earth
    val term3 = 1.0 / (1.0 - 0.0026 * cos(2 * (latitude ?: 45.0) * Math.PI / 180))

    // adjustment for variation of gravitational acceleration with height
    val term4 = 1.0 + (altitude / 6367324)

    return (10.0).pow(log10(barometricPressure) + altitude / (18400.0 * term1 * term2 * term3 * term4))
}
