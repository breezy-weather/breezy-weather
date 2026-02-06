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
import org.breezyweather.unit.temperature.Temperature

/**
 * TODO: Use our typed values
 * Compute pollutant concentration in µg/m³ when given in ppb.
 * Can also be used for converting to mg/m³ from ppm.
 * Source: https://en.wikipedia.org/wiki/Useful_conversions_and_formulas_for_air_dispersion_modeling
 *
 * Basis for temperature and pressure assumptions:
 * https://www.ecfr.gov/current/title-40/chapter-I/subchapter-C/part-50/section-50.3
 *
 * @param molecularMass
 * @param concentrationInPpb in ppb
 * @param temperature assumed 25 °C if omitted
 * @param barometricPressure assumed 1 atm = 1013.25 hPa if omitted
 */
fun computePollutantInUgm3FromPpb(
    molecularMass: Double?,
    concentrationInPpb: Double?,
    temperature: Temperature? = null,
    barometricPressure: Pressure? = null,
): Double? {
    if (concentrationInPpb == null) return null
    if (molecularMass == null) return null
    return concentrationInPpb *
        molecularMass /
        (8.31446261815324 / (barometricPressure?.inHectopascals ?: 1013.25) * 10) /
        (273.15 + (temperature?.inCelsius ?: 25.0))
}

/**
 * TODO: Use our typed values
 * Compute pollutant concentration in ppb from µg/m³
 * Can also be used for converting to ppm from mg/m³
 * Source: https://en.wikipedia.org/wiki/Useful_conversions_and_formulas_for_air_dispersion_modeling
 *
 * Basis for temperature and pressure assumptions:
 * https://www.ecfr.gov/current/title-40/chapter-I/subchapter-C/part-50/section-50.3
 *
 * @param molecularMass
 * @param concentrationInUgm3 in µg/m³
 * @param temperature assumed 25 °C if omitted
 * @param barometricPressure assumed 1 atm = 1013.25 hPa if omitted
 */
fun computePollutantInPpbFromUgm3(
    molecularMass: Double?,
    concentrationInUgm3: Double?,
    temperature: Temperature? = null,
    barometricPressure: Pressure? = null,
): Double? {
    if (concentrationInUgm3 == null) return null
    if (molecularMass == null) return null
    return concentrationInUgm3 /
        molecularMass *
        (8.31446261815324 / (barometricPressure?.inHectopascals ?: 1013.25) * 10) *
        (273.15 + (temperature?.inCelsius ?: 25.0))
}
