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

package org.breezyweather.unit.distance

import android.content.Context
import android.icu.util.MeasureUnit
import org.breezyweather.unit.R
import org.breezyweather.unit.WeatherUnit
import org.breezyweather.unit.formatting.UnitDecimals
import org.breezyweather.unit.formatting.UnitTranslation
import org.breezyweather.unit.formatting.UnitWidth
import org.breezyweather.unit.supportsMeasureUnit
import java.util.Locale

enum class DistanceUnit(
    override val id: String,
    override val displayName: UnitTranslation,
    override val nominative: UnitTranslation,
    override val per: UnitTranslation? = null,
    override val measureUnit: MeasureUnit?,
    override val perMeasureUnit: MeasureUnit? = null,
    val convertFromReference: (Double) -> Double,
    val convertToReference: (Double) -> Double,
    override val decimals: UnitDecimals,
    val chartStep: (Double) -> Double,
) : WeatherUnit {

    METER(
        id = "m",
        displayName = UnitTranslation(
            short = R.string.length_m_display_name_short,
            long = R.string.length_m_display_name_long
        ),
        nominative = UnitTranslation(
            short = R.string.length_m_nominative_short,
            long = R.string.length_m_nominative_long
        ),
        measureUnit = if (supportsMeasureUnit()) MeasureUnit.METER else null,
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit },
        convertToReference = { valueInThisUnit -> valueInThisUnit },
        decimals = UnitDecimals(0),
        chartStep = { maxY -> if (maxY < 40000) 5000.0 else 10000.0 }
    ),
    KILOMETER(
        id = "km",
        displayName = UnitTranslation(
            short = R.string.length_km_display_name_short,
            long = R.string.length_km_display_name_long
        ),
        nominative = UnitTranslation(
            short = R.string.length_km_nominative_short,
            long = R.string.length_km_nominative_long
        ),
        measureUnit = if (supportsMeasureUnit()) MeasureUnit.KILOMETER else null,
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit.div(1000.0) },
        convertToReference = { valueInThisUnit -> valueInThisUnit.times(1000.0) },
        decimals = UnitDecimals(narrow = 0, short = 1, long = 2),
        chartStep = { maxY -> if (maxY < 4) 5.0 else 10.0 }
    ),
    MILE(
        id = "mi",
        displayName = UnitTranslation(
            short = R.string.length_mi_display_name_short,
            long = R.string.length_mi_display_name_long
        ),
        nominative = UnitTranslation(
            short = R.string.length_mi_nominative_short,
            long = R.string.length_mi_nominative_long
        ),
        measureUnit = if (supportsMeasureUnit()) MeasureUnit.MILE else null,
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit.div(1609.344) },
        convertToReference = { valueInThisUnit -> valueInThisUnit.times(1609.344) },
        decimals = UnitDecimals(narrow = 0, short = 1, long = 2),
        chartStep = { maxY ->
            with(maxY) {
                when {
                    this <= 15.0 -> 3.0
                    this in 15.0..30.0 -> 5.0
                    else -> 30.0
                }
            }
        }
    ),
    NAUTICAL_MILE(
        id = "nmi",
        displayName = UnitTranslation(
            short = R.string.length_nmi_display_name_short,
            long = R.string.length_nmi_display_name_long
        ),
        nominative = UnitTranslation(
            short = R.string.length_nmi_nominative_short,
            long = R.string.length_nmi_nominative_long
        ),
        measureUnit = if (supportsMeasureUnit()) MeasureUnit.NAUTICAL_MILE else null,
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit.div(1852.0) },
        convertToReference = { valueInThisUnit -> valueInThisUnit.times(1852.0) },
        decimals = UnitDecimals(narrow = 0, short = 1, long = 2),
        chartStep = { maxY ->
            with(maxY) {
                when {
                    this <= 15.0 -> 3.0
                    this in 15.0..30.0 -> 5.0
                    else -> 30.0
                }
            }
        }
    ),
    FOOT(
        id = "ft",
        displayName = UnitTranslation(
            short = R.string.length_ft_display_name_short,
            long = R.string.length_ft_display_name_long
        ),
        nominative = UnitTranslation(
            short = R.string.length_ft_nominative_short,
            long = R.string.length_ft_nominative_long
        ),
        measureUnit = if (supportsMeasureUnit()) MeasureUnit.FOOT else null,
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit.times(3.28084) },
        convertToReference = { valueInThisUnit -> valueInThisUnit.div(3.28084) },
        decimals = UnitDecimals(0),
        chartStep = { maxY -> if (maxY < 150000) 25000.0 else 50000.0 }
    ),
    ;

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

        /**
         * Copyright © 1991-Present Unicode, Inc.
         * License: Unicode License v3 https://www.unicode.org/license.txt
         * Source (simplified): https://github.com/unicode-org/cldr/blob/3f3967f3cbadc56bbb44a9aed20784e82ac64c67/common/supplemental/units.xml#L506-L512
         */
        fun getDefaultUnit(
            locale: Locale = Locale.getDefault(),
        ) = when (locale.country) {
            "DE", "NL" -> METER
            "GB", "US" -> MILE
            else -> KILOMETER
        }

        fun getUnit(id: String): DistanceUnit? {
            return entries.firstOrNull { it.id == id }
        }
    }
}

/** Converts the given time distance [value] expressed in the specified [sourceUnit] into the specified [targetUnit]. */
internal fun convertDistanceUnit(
    value: Double,
    sourceUnit: DistanceUnit,
    targetUnit: DistanceUnit,
): Double {
    return if (sourceUnit == DistanceUnit.METER) {
        targetUnit.convertFromReference(value)
    } else if (targetUnit == DistanceUnit.METER) {
        sourceUnit.convertToReference(value)
    } else {
        targetUnit.convertFromReference(sourceUnit.convertToReference(value))
    }
}
