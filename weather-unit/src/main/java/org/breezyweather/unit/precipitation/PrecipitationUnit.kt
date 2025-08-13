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

package org.breezyweather.unit.precipitation

import android.icu.util.MeasureUnit
import android.os.Build
import org.breezyweather.unit.R
import org.breezyweather.unit.WeatherUnit
import org.breezyweather.unit.formatting.UnitDecimals
import org.breezyweather.unit.formatting.UnitTranslation
import java.util.Locale

enum class PrecipitationUnit(
    override val id: String,
    override val displayName: UnitTranslation,
    override val nominative: UnitTranslation,
    override val per: UnitTranslation? = null,
    override val measureUnit: MeasureUnit?,
    override val perMeasureUnit: MeasureUnit?,
    val convertFromReference: (Double) -> Double,
    val convertToReference: (Double) -> Double,
    override val decimals: UnitDecimals,
    val chartStep: Double,
) : WeatherUnit {

    MICROMETER(
        id = "microm",
        displayName = UnitTranslation(
            short = R.string.length_microm_display_name_short,
            long = R.string.length_microm_display_name_long
        ),
        nominative = UnitTranslation(
            short = R.string.length_microm_nominative_short,
            long = R.string.length_microm_nominative_long
        ),
        measureUnit = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.MICROMETER else null,
        perMeasureUnit = null,
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit },
        convertToReference = { valueInDefaultUnit -> valueInDefaultUnit },
        decimals = UnitDecimals(0),
        chartStep = 5000.0
    ),
    MILLIMETER(
        id = "mm",
        displayName = UnitTranslation(
            short = R.string.length_mm_display_name_short,
            long = R.string.length_mm_display_name_long
        ),
        nominative = UnitTranslation(
            short = R.string.length_mm_nominative_short,
            long = R.string.length_mm_nominative_long
        ),
        measureUnit = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.MILLIMETER else null,
        perMeasureUnit = null,
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit.div(1000.0) },
        convertToReference = { valueInDefaultUnit -> valueInDefaultUnit.times(1000.0) },
        decimals = UnitDecimals(narrow = 0, short = 1, long = 2),
        chartStep = 5.0
    ),
    CENTIMETER(
        id = "cm",
        displayName = UnitTranslation(
            short = R.string.length_cm_display_name_short,
            long = R.string.length_cm_display_name_long
        ),
        nominative = UnitTranslation(
            short = R.string.length_cm_nominative_short,
            long = R.string.length_cm_nominative_long
        ),
        measureUnit = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.CENTIMETER else null,
        perMeasureUnit = null,
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit.div(10000.0) },
        convertToReference = { valueInDefaultUnit -> valueInDefaultUnit.times(10000.0) },
        decimals = UnitDecimals(narrow = 1, short = 2, long = 3),
        chartStep = 0.5
    ),
    INCH(
        id = "in",
        displayName = UnitTranslation(
            short = R.string.length_in_display_name_short,
            long = R.string.length_in_display_name_long
        ),
        nominative = UnitTranslation(
            short = R.string.length_in_nominative_short,
            long = R.string.length_in_nominative_long
        ),
        measureUnit = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.INCH else null,
        perMeasureUnit = null,
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit.div(25400.0) },
        convertToReference = { valueInDefaultUnit -> valueInDefaultUnit.times(25400.0) },
        decimals = UnitDecimals(narrow = 1, short = 2, long = 3),
        chartStep = 0.2
    ),
    LITER_PER_SQUARE_METER( // Is actually the same as millimeters, just expressed differently
        id = "lpsqm",
        displayName = UnitTranslation(
            short = R.string.volume_l_display_name_short,
            long = R.string.volume_l_display_name_long
        ),
        nominative = UnitTranslation(
            short = R.string.volume_l_nominative_short,
            long = R.string.volume_l_nominative_long
        ),
        per = UnitTranslation(
            short = R.string.length_m2_per_short,
            long = R.string.length_m2_per_long
        ),
        measureUnit = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.LITER else null,
        perMeasureUnit = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MeasureUnit.SQUARE_METER else null,
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit.div(1000.0) },
        convertToReference = { valueInDefaultUnit -> valueInDefaultUnit.times(1000.0) },
        decimals = UnitDecimals(narrow = 0, short = 1, long = 2),
        chartStep = 5.0
    ),
    ;

    companion object {

        /**
         * Copyright © 1991-Present Unicode, Inc.
         * License: Unicode License v3 https://www.unicode.org/license.txt
         * Source (simplified): https://github.com/unicode-org/cldr/blob/3f3967f3cbadc56bbb44a9aed20784e82ac64c67/common/supplemental/units.xml#L474-L478
         */
        fun getDefaultUnit(
            locale: Locale,
        ) = when (locale.country) {
            "BR" -> CENTIMETER
            "US" -> INCH
            else -> MILLIMETER
        }

        /**
         * Copyright © 1991-Present Unicode, Inc.
         * License: Unicode License v3 https://www.unicode.org/license.txt
         * Source (simplified): https://github.com/unicode-org/cldr/blob/3f3967f3cbadc56bbb44a9aed20784e82ac64c67/common/supplemental/units.xml#L498-L501
         */
        fun getDefaultSnowfallUnit(
            locale: Locale,
        ) = when (locale.country) {
            "US" -> INCH
            else -> CENTIMETER
        }

        fun getUnit(id: String): PrecipitationUnit? {
            return entries.firstOrNull { it.id == id }
        }
    }
}

/** Converts the given time precipitation [value] expressed in the specified [sourceUnit] into the specified [targetUnit]. */
internal fun convertPrecipitationUnit(
    value: Double,
    sourceUnit: PrecipitationUnit,
    targetUnit: PrecipitationUnit,
): Double {
    return if (sourceUnit == PrecipitationUnit.MICROMETER) {
        targetUnit.convertFromReference(value)
    } else if (targetUnit == PrecipitationUnit.MICROMETER) {
        sourceUnit.convertToReference(value)
    } else {
        targetUnit.convertFromReference(sourceUnit.convertToReference(value))
    }
}
