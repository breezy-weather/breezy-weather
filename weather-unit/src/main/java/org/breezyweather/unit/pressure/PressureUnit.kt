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

import android.content.Context
import android.icu.util.MeasureUnit
import android.os.Build
import androidx.annotation.RequiresApi
import org.breezyweather.unit.R
import org.breezyweather.unit.WeatherUnit
import org.breezyweather.unit.formatting.UnitDecimals
import org.breezyweather.unit.formatting.UnitTranslation
import org.breezyweather.unit.formatting.UnitWidth
import org.breezyweather.unit.supportsMeasureUnitAtmosphere
import java.util.Locale

enum class PressureUnit(
    override val id: String,
    override val displayName: UnitTranslation,
    override val nominative: UnitTranslation,
    override val per: UnitTranslation? = null,
    val convertFromReference: (Double) -> Double,
    val convertToReference: (Double) -> Double,
    override val decimals: UnitDecimals,
    val chartStep: Double,
) : WeatherUnit {

    PASCAL(
        id = "pa",
        displayName = UnitTranslation(
            short = R.string.pressure_pa_display_name_short,
            long = R.string.pressure_pa_display_name_long
        ),
        nominative = UnitTranslation(
            short = R.string.pressure_pa_nominative_short,
            long = R.string.pressure_pa_nominative_long
        ),
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit },
        convertToReference = { valueInThisUnit -> valueInThisUnit },
        decimals = UnitDecimals(0),
        chartStep = 15.0
    ),
    HECTOPASCAL(
        id = "hpa",
        displayName = UnitTranslation(
            short = R.string.pressure_hpa_display_name_short,
            long = R.string.pressure_hpa_display_name_long
        ),
        nominative = UnitTranslation(
            short = R.string.pressure_hpa_nominative_short,
            long = R.string.pressure_hpa_nominative_long
        ),
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit.div(100.0) },
        convertToReference = { valueInThisUnit -> valueInThisUnit.times(100.0) },
        decimals = UnitDecimals(narrow = 0, short = 1, long = 2),
        chartStep = 15.0
    ),
    KILOPASCAL(
        id = "kpa",
        displayName = UnitTranslation(
            short = R.string.pressure_kpa_display_name_short,
            long = R.string.pressure_kpa_display_name_long
        ),
        nominative = UnitTranslation(
            short = R.string.pressure_kpa_nominative_short,
            long = R.string.pressure_kpa_nominative_long
        ),
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit.div(1000.0) },
        convertToReference = { valueInThisUnit -> valueInThisUnit.times(1000.0) },
        decimals = UnitDecimals(narrow = 1, short = 2, long = 3),
        chartStep = 1.5
    ),
    MILLIBAR(
        id = "mb",
        displayName = UnitTranslation(
            short = R.string.pressure_mbar_display_name_short,
            long = R.string.pressure_mbar_display_name_long
        ),
        nominative = UnitTranslation(
            short = R.string.pressure_mbar_nominative_short,
            long = R.string.pressure_mbar_nominative_long
        ),
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit.div(100.0) },
        convertToReference = { valueInThisUnit -> valueInThisUnit.times(100.0) },
        decimals = UnitDecimals(narrow = 0, short = 1, long = 2),
        chartStep = 15.0
    ),
    ATMOSPHERE(
        id = "atm",
        displayName = UnitTranslation(
            short = R.string.pressure_atm_display_name_short,
            long = R.string.pressure_atm_display_name_long
        ),
        nominative = UnitTranslation(
            short = R.string.pressure_atm_nominative_short,
            long = R.string.pressure_atm_nominative_long
        ),
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit.div(101325.0) },
        convertToReference = { valueInThisUnit -> valueInThisUnit.times(101325.0) },
        decimals = UnitDecimals(narrow = 1, short = 2, long = 3),
        chartStep = 0.015
    ),
    MILLIMETER_OF_MERCURY(
        id = "mmhg",
        displayName = UnitTranslation(
            short = R.string.pressure_mmhg_display_name_short,
            long = R.string.pressure_mmhg_display_name_long
        ),
        nominative = UnitTranslation(
            short = R.string.pressure_mmhg_nominative_short,
            long = R.string.pressure_mmhg_nominative_long
        ),
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit.times(760.0 / 101325.0) },
        convertToReference = { valueInThisUnit -> valueInThisUnit.times(101325.0 / 760.0) },
        decimals = UnitDecimals(narrow = 0, short = 1, long = 2),
        chartStep = 10.0
    ),
    INCH_OF_MERCURY(
        id = "inhg",
        displayName = UnitTranslation(
            short = R.string.pressure_inhg_display_name_short,
            long = R.string.pressure_inhg_display_name_long
        ),
        nominative = UnitTranslation(
            short = R.string.pressure_inhg_nominative_short,
            long = R.string.pressure_inhg_nominative_long
        ),
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit.times(760.0 / 101325.0).div(25.4) },
        convertToReference = { valueInThisUnit -> valueInThisUnit.times(25.4).times(101325.0 / 760.0) },
        decimals = UnitDecimals(narrow = 1, short = 2, long = 3),
        chartStep = 0.5
    ),
    ;

    @RequiresApi(Build.VERSION_CODES.N)
    override fun getMeasureUnit(): MeasureUnit? {
        return when (this) {
            PASCAL -> null
            HECTOPASCAL -> MeasureUnit.HECTOPASCAL
            KILOPASCAL -> null
            MILLIBAR -> MeasureUnit.MILLIBAR
            ATMOSPHERE -> if (supportsMeasureUnitAtmosphere()) MeasureUnit.ATMOSPHERE else null
            MILLIMETER_OF_MERCURY -> MeasureUnit.MILLIMETER_OF_MERCURY
            INCH_OF_MERCURY -> MeasureUnit.INCH_HG
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun getPerMeasureUnit(): MeasureUnit? = null

    /**
     * Override to:
     * - Use English units with Traditional Chinese
     */
    override fun format(
        context: Context,
        value: Number,
        valueWidth: UnitWidth,
        unitWidth: UnitWidth,
        locale: Locale,
        showSign: Boolean,
        useNumberFormatter: Boolean,
        useMeasureFormat: Boolean,
    ): String {
        // Translations missing for Esperanto in CLDR
        if (locale.language.equals("eo", ignoreCase = true)) {
            return formatWithAndroidTranslations(
                context = context,
                value = value,
                valueWidth = valueWidth,
                unitWidth = unitWidth,
                locale = locale,
                showSign = showSign,
                useNumberFormatter = useNumberFormatter,
                useMeasureFormat = useMeasureFormat
            )
        }

        val correctedLocale = locale.let {
            /**
             * Taiwan guidelines: https://www.bsmi.gov.tw/wSite/public/Attachment/f1736149048776.pdf
             * Ongoing issue: https://unicode-org.atlassian.net/jira/software/c/projects/CLDR/issues/CLDR-10604
             */
            if (it.language.equals("zh", ignoreCase = true) &&
                arrayOf("TW", "HK", "MO").any { c -> it.country.equals(c, ignoreCase = true) } &&
                unitWidth != UnitWidth.LONG
            ) {
                Locale.Builder().setLanguage("en").setRegion("001").build()
            } else {
                it
            }
        }
        return super.format(
            context = context,
            value = value,
            valueWidth = valueWidth,
            unitWidth = unitWidth,
            locale = correctedLocale,
            showSign = showSign,
            useNumberFormatter = useNumberFormatter,
            useMeasureFormat = useNumberFormatter
        )
    }

    companion object {

        const val STANDARD = 101325

        /**
         * Copyright © 1991-Present Unicode, Inc.
         * License: Unicode License v3 https://www.unicode.org/license.txt
         * Source (simplified): https://github.com/unicode-org/cldr/blob/3f3967f3cbadc56bbb44a9aed20784e82ac64c67/common/supplemental/units.xml#L546-L551
         *
         * Breezy updates:
         * - Canada uses kPa
         */
        fun getDefaultUnit(
            locale: Locale = Locale.getDefault(),
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
