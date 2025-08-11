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

package org.breezyweather.unit.pressure

import android.icu.util.MeasureUnit
import android.os.Build
import org.breezyweather.unit.R
import org.breezyweather.unit.WeatherUnit
import org.breezyweather.unit.formatting.UnitDecimals
import org.breezyweather.unit.formatting.UnitDisplayName
import org.breezyweather.unit.formatting.UnitNominative
import java.util.Locale

enum class PressureUnit(
    override val id: String,
    override val displayName: UnitDisplayName,
    override val nominative: UnitNominative,
    override val measureUnit: MeasureUnit?,
    override val perMeasureUnit: MeasureUnit?,
    override val convertFromReference: (Double) -> Double,
    override val convertToReference: (Double) -> Double,
    override val decimals: UnitDecimals,
    override val chartStep: Double,
) : WeatherUnit {

    PASCAL(
        "pa",
        displayName = UnitDisplayName(
            short = R.string.pressure_pa_display_name_short,
            long = R.string.pressure_pa_display_name_long
        ),
        nominative = UnitNominative(
            short = R.string.pressure_pa_nominative_short,
            long = R.string.pressure_pa_nominative_long
        ),
        null,
        null,
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit },
        convertToReference = { valueInDefaultUnit -> valueInDefaultUnit },
        decimals = UnitDecimals(0),
        chartStep = 15.0
    ),
    HECTOPASCAL(
        "hpa",
        displayName = UnitDisplayName(
            short = R.string.pressure_hpa_display_name_short,
            long = R.string.pressure_hpa_display_name_long
        ),
        nominative = UnitNominative(
            short = R.string.pressure_hpa_nominative_short,
            long = R.string.pressure_hpa_nominative_long
        ),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.HECTOPASCAL else null,
        null,
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit.div(100.0) },
        convertToReference = { valueInDefaultUnit -> valueInDefaultUnit.times(100.0) },
        decimals = UnitDecimals(narrow = 0, short = 1, long = 2),
        chartStep = 15.0
    ),
    KILOPASCAL(
        "kpa",
        displayName = UnitDisplayName(
            short = R.string.pressure_kpa_display_name_short,
            long = R.string.pressure_kpa_display_name_long
        ),
        nominative = UnitNominative(
            short = R.string.pressure_kpa_nominative_short,
            long = R.string.pressure_kpa_nominative_long
        ),
        null,
        null,
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit.div(1000.0) },
        convertToReference = { valueInDefaultUnit -> valueInDefaultUnit.times(1000.0) },
        decimals = UnitDecimals(narrow = 1, short = 2, long = 3),
        chartStep = 1.5
    ),
    MILLIBAR(
        "mb",
        displayName = UnitDisplayName(
            short = R.string.pressure_mbar_display_name_short,
            long = R.string.pressure_mbar_display_name_long
        ),
        nominative = UnitNominative(
            short = R.string.pressure_mbar_nominative_short,
            long = R.string.pressure_mbar_nominative_long
        ),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.MILLIBAR else null,
        null,
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit.div(100.0) },
        convertToReference = { valueInDefaultUnit -> valueInDefaultUnit.times(100.0) },
        decimals = UnitDecimals(narrow = 0, short = 1, long = 2),
        chartStep = 15.0
    ),
    ATMOSPHERE(
        "atm",
        displayName = UnitDisplayName(
            short = R.string.pressure_atm_display_name_short,
            long = R.string.pressure_atm_display_name_long
        ),
        nominative = UnitNominative(
            short = R.string.pressure_atm_nominative_short,
            long = R.string.pressure_atm_nominative_long
        ),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) MeasureUnit.ATMOSPHERE else null,
        null,
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit.div(101325.0) },
        convertToReference = { valueInDefaultUnit -> valueInDefaultUnit.times(101325.0) },
        decimals = UnitDecimals(narrow = 1, short = 2, long = 3),
        chartStep = 0.015
    ),
    MILLIMETER_OF_MERCURY(
        "mmhg",
        displayName = UnitDisplayName(
            short = R.string.pressure_mmhg_display_name_short,
            long = R.string.pressure_mmhg_display_name_long
        ),
        nominative = UnitNominative(
            short = R.string.pressure_mmhg_nominative_short,
            long = R.string.pressure_mmhg_nominative_long
        ),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.MILLIMETER_OF_MERCURY else null,
        null,
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit.times(760.0 / 101325.0) },
        convertToReference = { valueInDefaultUnit -> valueInDefaultUnit.times(101325.0 / 760.0) },
        decimals = UnitDecimals(narrow = 0, short = 1, long = 2),
        chartStep = 10.0
    ),
    INCH_OF_MERCURY(
        "inhg",
        displayName = UnitDisplayName(
            short = R.string.pressure_inhg_display_name_short,
            long = R.string.pressure_inhg_display_name_long
        ),
        nominative = UnitNominative(
            short = R.string.pressure_inhg_nominative_short,
            long = R.string.pressure_inhg_nominative_long
        ),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.INCH_HG else null,
        null,
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit.times(760.0 / 101325.0).div(25.4) },
        convertToReference = { valueInDefaultUnit -> valueInDefaultUnit.times(25.4).times(101325.0 / 760.0) },
        decimals = UnitDecimals(narrow = 1, short = 2, long = 3),
        chartStep = 0.5
    ),
    ;

    companion object {

        const val NORMAL = 101325

        /**
         * Copyright © 1991-Present Unicode, Inc.
         * License: Unicode License v3 https://www.unicode.org/license.txt
         * Source (simplified): https://github.com/unicode-org/cldr/blob/3f3967f3cbadc56bbb44a9aed20784e82ac64c67/common/supplemental/units.xml#L546-L551
         *
         * Breezy updates:
         * - Canada uses kPa
         */
        fun getDefaultUnit(
            locale: Locale,
        ) = when (locale.country) {
            "BR", "EG", "GB", "IL", "TH" -> MILLIBAR
            "MX", "RU" -> MILLIMETER_OF_MERCURY
            "US" -> INCH_OF_MERCURY
            "CA" -> KILOPASCAL
            else -> HECTOPASCAL
        }

        fun getUnit(id: String): PressureUnit? {
            return entries.firstOrNull { it.id == id }
        }
    }
}

/** Converts the given time pressure [value] expressed in the specified [sourceUnit] into the specified [targetUnit]. */
internal fun convertPressureUnit(
    value: Double,
    sourceUnit: PressureUnit,
    targetUnit: PressureUnit,
): Double {
    return if (sourceUnit == PressureUnit.PASCAL) {
        targetUnit.convertFromReference(value)
    } else if (targetUnit == PressureUnit.PASCAL) {
        sourceUnit.convertToReference(value)
    } else {
        targetUnit.convertFromReference(sourceUnit.convertToReference(value))
    }
}
