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

package org.breezyweather.unit.pollutant

import android.icu.util.MeasureUnit
import android.os.Build
import org.breezyweather.unit.R
import org.breezyweather.unit.WeatherUnit
import org.breezyweather.unit.formatting.UnitDecimals
import org.breezyweather.unit.formatting.UnitTranslation

enum class PollutantConcentrationUnit(
    override val id: String,
    override val displayName: UnitTranslation,
    override val nominative: UnitTranslation,
    override val per: UnitTranslation? = UnitTranslation(
        short = R.string.length_m3_per_short,
        long = R.string.length_m3_per_long
    ),
    override val measureUnit: MeasureUnit?,
    override val perMeasureUnit: MeasureUnit? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        MeasureUnit.CUBIC_METER
    } else {
        null
    },
    val convertFromReference: (Double) -> Double,
    val convertToReference: (Double) -> Double,
    override val decimals: UnitDecimals,
    val chartStep: Double,
) : WeatherUnit {

    MICROGRAM_PER_CUBIC_METER(
        "microgpcum",
        displayName = UnitTranslation(
            short = R.string.weight_microg_display_name_short,
            long = R.string.weight_microg_display_name_long
        ),
        nominative = UnitTranslation(
            short = R.string.weight_microg_nominative_short,
            long = R.string.weight_microg_nominative_long
        ),
        measureUnit = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.MICROGRAM else null,
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit },
        convertToReference = { valueInDefaultUnit -> valueInDefaultUnit },
        decimals = UnitDecimals(0),
        chartStep = 15.0 // TODO
    ),
    MILLIGRAM_PER_CUBIC_METER(
        "mgpcum",
        displayName = UnitTranslation(
            short = R.string.weight_mg_display_name_short,
            long = R.string.weight_mg_display_name_long
        ),
        nominative = UnitTranslation(
            short = R.string.weight_mg_nominative_short,
            long = R.string.weight_mg_nominative_long
        ),
        measureUnit = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.MILLIGRAM else null,
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit.div(1000.0) },
        convertToReference = { valueInDefaultUnit -> valueInDefaultUnit.times(1000.0) },
        decimals = UnitDecimals(short = 0, long = 1),
        chartStep = 15.0 // TODO
    ),
    ;

    companion object {

        fun getUnit(id: String): PollutantConcentrationUnit? {
            return entries.firstOrNull { it.id == id }
        }
    }
}

/** Converts the given time pollutant concentration [value] expressed in the specified [sourceUnit] into the specified [targetUnit]. */
internal fun convertPollutantConcentrationUnit(
    value: Double,
    sourceUnit: PollutantConcentrationUnit,
    targetUnit: PollutantConcentrationUnit,
): Double {
    return if (sourceUnit == PollutantConcentrationUnit.MICROGRAM_PER_CUBIC_METER) {
        targetUnit.convertFromReference(value)
    } else if (targetUnit == PollutantConcentrationUnit.MICROGRAM_PER_CUBIC_METER) {
        sourceUnit.convertToReference(value)
    } else {
        targetUnit.convertFromReference(sourceUnit.convertToReference(value))
    }
}
