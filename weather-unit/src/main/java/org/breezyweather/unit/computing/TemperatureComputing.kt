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
 * Compute apparent temperature from temperature, relative humidity, and wind speed
 * Uses Bureau of Meteorology Australia methodology
 * Source: http://www.bom.gov.au/info/thermal_stress/#atapproximation
 * TODO: Unit test
 *
 * @param temperature
 * @param relativeHumidity
 * @param windSpeed
 */
fun computeApparentTemperature(
    temperature: Temperature?,
    relativeHumidity: Ratio?,
    windSpeed: Speed?,
): Temperature? {
    if (temperature == null || relativeHumidity == null || windSpeed == null) return null

    val e = relativeHumidity.inFraction * 6.105 * exp(17.27 * temperature.inCelsius / (237.7 + temperature.inCelsius))
    return (temperature.inCelsius + 0.33 * e - 0.7 * windSpeed.inMetersPerSecond - 4.0).celsius
}

/**
 * Compute wind chill from temperature and wind speed
 * Uses Environment Canada methodology
 * Source: https://climate.weather.gc.ca/glossary_e.html#w
 * Only valid for (T ≤ 0 °C) or (T ≤ 10°C and WS ≥ 5 km/h)
 * TODO: Unit test
 *
 * @param temperature
 * @param windSpeed
 */
fun computeWindChillTemperature(
    temperature: Temperature?,
    windSpeed: Speed?,
): Temperature? {
    if (temperature == null || windSpeed == null || temperature > 10.celsius) return null
    return if (windSpeed >= 5.kilometersPerHour) {
        (
            13.12 +
                (0.6215 * temperature.inCelsius) -
                (11.37 * windSpeed.inKilometersPerHour.pow(0.16)) +
                (0.3965 * temperature.inCelsius * windSpeed.inKilometersPerHour.pow(0.16))
            ).celsius
    } else if (temperature <= 0.celsius) {
        (temperature.inCelsius + ((-1.59 + 0.1345 * temperature.inCelsius) / 5.0) * windSpeed.inKilometersPerHour)
            .celsius
    } else {
        null
    }
}

/**
 * Compute humidex from temperature and humidity
 * Based on formula from ECCC
 *
 * @param temperature
 * @param dewPoint
 */
fun computeHumidex(
    temperature: Temperature?,
    dewPoint: Temperature?,
): Temperature? {
    if (temperature == null || dewPoint == null || temperature < 15.celsius) return null

    return (
        temperature.inCelsius +
            0.5555.times(
                6.11.times(
                    exp(5417.7530.times(1.div(273.15) - 1.div(273.15 + dewPoint.inCelsius)))
                ).minus(10)
            )
        ).celsius
}
