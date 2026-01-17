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

package org.breezyweather.unit.temperature

import android.content.Context
import android.icu.util.MeasureUnit
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.text.util.LocalePreferences
import org.breezyweather.unit.R
import org.breezyweather.unit.WeatherUnit
import org.breezyweather.unit.formatting.UnitDecimals
import org.breezyweather.unit.formatting.UnitTranslation
import org.breezyweather.unit.formatting.UnitWidth
import java.util.Locale

enum class TemperatureUnit(
    override val id: String,
    override val displayName: UnitTranslation,
    override val nominative: UnitTranslation,
    override val per: UnitTranslation? = null,
    val convertFromReference: (Double) -> Double,
    val convertToReference: (Double) -> Double,
    val convertDeviationFromReference: (Double) -> Double,
    val convertDeviationToReference: (Double) -> Double,
    override val decimals: UnitDecimals,
    val chartStep: Double,
) : WeatherUnit {

    DECI_CELSIUS(
        id = "dc",
        displayName = UnitTranslation(
            short = R.string.temperature_dc_display_name_short,
            long = R.string.temperature_dc_display_name_long
        ),
        nominative = UnitTranslation(
            narrow = R.string.temperature_dc_nominative_narrow,
            short = R.string.temperature_dc_nominative_short,
            long = R.string.temperature_dc_nominative_long
        ),
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit },
        convertToReference = { valueInThisUnit -> valueInThisUnit },
        convertDeviationFromReference = { valueInDefaultUnit -> valueInDefaultUnit },
        convertDeviationToReference = { valueInThisUnit -> valueInThisUnit },
        decimals = UnitDecimals(0),
        chartStep = 50.0
    ),
    CELSIUS(
        id = "c",
        displayName = UnitTranslation(
            short = R.string.temperature_c_display_name_short,
            long = R.string.temperature_c_display_name_long
        ),
        nominative = UnitTranslation(
            narrow = R.string.temperature_c_nominative_narrow,
            short = R.string.temperature_c_nominative_short,
            long = R.string.temperature_c_nominative_long
        ),
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit.div(10.0) },
        convertToReference = { valueInThisUnit -> valueInThisUnit.times(10.0) },
        convertDeviationFromReference = { valueInDefaultUnit -> valueInDefaultUnit.div(10.0) },
        convertDeviationToReference = { valueInThisUnit -> valueInThisUnit.times(10.0) },
        decimals = UnitDecimals(narrow = 0, short = 1, long = 1),
        chartStep = 5.0
    ),
    FAHRENHEIT(
        id = "f",
        displayName = UnitTranslation(
            short = R.string.temperature_f_display_name_short,
            long = R.string.temperature_f_display_name_long
        ),
        nominative = UnitTranslation(
            narrow = R.string.temperature_f_nominative_narrow,
            short = R.string.temperature_f_nominative_short,
            long = R.string.temperature_f_nominative_long
        ),
        convertFromReference = { valueInDefaultUnit -> 9.0.div(5.0).times(valueInDefaultUnit.div(10.0)) + 32 },
        convertToReference = { valueInThisUnit -> 5.0.div(9.0).times(valueInThisUnit - 32.0).times(10.0) },
        convertDeviationFromReference = { valueInDefaultUnit -> 9.0.div(5.0).times(valueInDefaultUnit.div(10.0)) },
        convertDeviationToReference = { valueInThisUnit -> 5.0.div(9.0).times(valueInThisUnit).times(10.0) },
        decimals = UnitDecimals(narrow = 0, short = 0, long = 1),
        chartStep = 10.0
    ),
    KELVIN(
        id = "k",
        displayName = UnitTranslation(
            short = R.string.temperature_k_display_name_short,
            long = R.string.temperature_k_display_name_long
        ),
        nominative = UnitTranslation(
            narrow = R.string.temperature_k_nominative_narrow,
            short = R.string.temperature_k_nominative_short,
            long = R.string.temperature_k_nominative_long
        ),
        convertFromReference = { valueInDefaultUnit -> 273.15 + valueInDefaultUnit.div(10.0) },
        convertToReference = { valueInThisUnit -> (valueInThisUnit - 273.15).times(10.0) },
        convertDeviationFromReference = { valueInDefaultUnit -> valueInDefaultUnit.div(10.0) },
        convertDeviationToReference = { valueInThisUnit -> valueInThisUnit.times(10.0) },
        decimals = UnitDecimals(narrow = 0, short = 1, long = 2),
        chartStep = 5.0
    ),
    ;

    @RequiresApi(Build.VERSION_CODES.N)
    override fun getMeasureUnit(): MeasureUnit? {
        return when (this) {
            DECI_CELSIUS -> null
            CELSIUS -> MeasureUnit.CELSIUS
            FAHRENHEIT -> MeasureUnit.FAHRENHEIT
            KELVIN -> MeasureUnit.KELVIN
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun getPerMeasureUnit(): MeasureUnit? = null

    /**
     * Override to:
     * - Never use ICU for narrow, so that it’s always displayed 13° instead of 13°C in some languages
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
        // Always use %s° for narrow temperature formatting
        // Translations missing for Esperanto in CLDR
        // Incorrect translations for Polish in CLDR (“st. C” is never used on Polish websites)
        if (unitWidth == UnitWidth.NARROW ||
            locale.language.equals("eo", ignoreCase = true) ||
            (locale.language.equals("pl", ignoreCase = true) && unitWidth == UnitWidth.SHORT)
        ) {
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
            /*
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
        /**
         * Resolve in the following order:
         * - System regional preference
         * - Current locale region preference
         *
         * Known issue: [LocalePreferences].[getTemperatureUnit()] is terribly slow, so avoid calling this often
         */
        fun getDefaultUnit(
            locale: Locale = Locale.getDefault(),
        ) = when (LocalePreferences.getTemperatureUnit()) {
            LocalePreferences.TemperatureUnit.CELSIUS -> CELSIUS
            LocalePreferences.TemperatureUnit.FAHRENHEIT -> FAHRENHEIT
            LocalePreferences.TemperatureUnit.KELVIN -> KELVIN
            /*
             * Copyright © 1991-Present Unicode, Inc.
             * License: Unicode License v3 https://www.unicode.org/license.txt
             * Source: https://github.com/unicode-org/cldr/blob/3f3967f3cbadc56bbb44a9aed20784e82ac64c67/common/supplemental/units.xml#L579-L582
             */
            else -> when (locale.country) {
                "BS", "BZ", "KY", "PR", "PW", "US" -> FAHRENHEIT
                else -> CELSIUS
            }
        }

        fun getUnit(id: String): TemperatureUnit? {
            return entries.firstOrNull { it.id == id }
        }
    }
}

/** Converts the given time temperature [value] expressed in the specified [sourceUnit] into the specified [targetUnit]. */
internal fun convertTemperatureUnit(
    value: Double,
    sourceUnit: TemperatureUnit,
    targetUnit: TemperatureUnit,
): Double {
    return if (sourceUnit == TemperatureUnit.DECI_CELSIUS) {
        targetUnit.convertFromReference(value)
    } else if (targetUnit == TemperatureUnit.DECI_CELSIUS) {
        sourceUnit.convertToReference(value)
    } else {
        targetUnit.convertFromReference(sourceUnit.convertToReference(value))
    }
}

/** Converts the given time temperature deviation [value] expressed in the specified [sourceUnit] into the specified [targetUnit]. */
internal fun convertTemperatureUnitDeviation(
    value: Double,
    sourceUnit: TemperatureUnit,
    targetUnit: TemperatureUnit,
): Double {
    return if (sourceUnit == TemperatureUnit.DECI_CELSIUS) {
        targetUnit.convertDeviationFromReference(value)
    } else if (targetUnit == TemperatureUnit.DECI_CELSIUS) {
        sourceUnit.convertDeviationToReference(value)
    } else {
        targetUnit.convertDeviationFromReference(sourceUnit.convertDeviationToReference(value))
    }
}
