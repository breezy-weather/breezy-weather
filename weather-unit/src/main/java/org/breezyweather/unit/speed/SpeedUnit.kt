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

package org.breezyweather.unit.speed

import android.content.Context
import android.icu.util.MeasureUnit
import android.os.Build
import androidx.annotation.RequiresApi
import org.breezyweather.unit.R
import org.breezyweather.unit.WeatherUnit
import org.breezyweather.unit.formatting.UnitDecimals
import org.breezyweather.unit.formatting.UnitTranslation
import org.breezyweather.unit.formatting.UnitWidth
import org.breezyweather.unit.supportsMeasureUnitBeaufort
import org.breezyweather.unit.supportsMeasureUnitKnot
import java.util.Locale

enum class SpeedUnit(
    override val id: String,
    override val displayName: UnitTranslation,
    override val nominative: UnitTranslation,
    override val per: UnitTranslation? = null,
    val convertFromReference: (Double) -> Double,
    val convertToReference: (Double) -> Double,
    override val decimals: UnitDecimals,
    val chartStep: Double,
) : WeatherUnit {

    CENTIMETER_PER_SECOND(
        id = "cmps",
        displayName = UnitTranslation(
            short = R.string.length_cm_display_name_short,
            long = R.string.length_cm_display_name_long
        ),
        nominative = UnitTranslation(
            short = R.string.length_cm_nominative_short,
            long = R.string.length_cm_nominative_long
        ),
        per = UnitTranslation(
            short = R.string.duration_sec_per_short,
            long = R.string.duration_sec_per_long
        ),
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit },
        convertToReference = { valueInThisUnit -> valueInThisUnit },
        decimals = UnitDecimals(0),
        chartStep = 50.0
    ),
    METER_PER_SECOND(
        id = "mps",
        displayName = UnitTranslation(
            short = R.string.length_m_display_name_short,
            long = R.string.length_m_display_name_long
        ),
        nominative = UnitTranslation(
            short = R.string.length_m_nominative_short,
            long = R.string.length_m_nominative_long
        ),
        per = UnitTranslation(
            short = R.string.duration_sec_per_short,
            long = R.string.duration_sec_per_long
        ),
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit.div(100.0) },
        convertToReference = { valueInThisUnit -> valueInThisUnit.times(100.0) },
        decimals = UnitDecimals(narrow = 0, short = 1, long = 2),
        chartStep = 0.5
    ),
    KILOMETER_PER_HOUR(
        id = "kph",
        displayName = UnitTranslation(
            short = R.string.length_km_display_name_short,
            long = R.string.length_km_display_name_long
        ),
        nominative = UnitTranslation(
            short = R.string.length_km_nominative_short,
            long = R.string.length_km_nominative_long
        ),
        per = UnitTranslation(
            short = R.string.duration_hr_per_short,
            long = R.string.duration_hr_per_long
        ),
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit.times(0.036) },
        convertToReference = { valueInThisUnit -> valueInThisUnit.div(0.036) },
        decimals = UnitDecimals(narrow = 0, short = 1, long = 2),
        chartStep = 15.0
    ),
    MILE_PER_HOUR(
        id = "mph",
        displayName = UnitTranslation(
            short = R.string.length_mi_display_name_short,
            long = R.string.length_mi_display_name_long
        ),
        nominative = UnitTranslation(
            short = R.string.length_mi_nominative_short,
            long = R.string.length_mi_nominative_long
        ),
        per = UnitTranslation(
            short = R.string.duration_hr_per_short,
            long = R.string.duration_hr_per_long
        ),
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit.div(44.704) },
        convertToReference = { valueInThisUnit -> valueInThisUnit.times(44.704) },
        decimals = UnitDecimals(narrow = 0, short = 1, long = 2),
        chartStep = 10.0
    ),
    KNOT(
        id = "kn",
        displayName = UnitTranslation(
            short = R.string.speed_kn_display_name_short,
            long = R.string.speed_kn_display_name_long
        ),
        nominative = UnitTranslation(
            short = R.string.speed_kn_nominative_short,
            long = R.string.speed_kn_nominative_long
        ),
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit.times(0.036).div(1.852) },
        convertToReference = { valueInThisUnit -> valueInThisUnit.times(1.852).div(0.036) },
        decimals = UnitDecimals(narrow = 0, short = 1, long = 2),
        chartStep = 10.0
    ),
    FOOT_PER_SECOND(
        id = "ftps",
        displayName = UnitTranslation(
            short = R.string.length_ft_display_name_short,
            long = R.string.length_ft_display_name_long
        ),
        nominative = UnitTranslation(
            short = R.string.length_ft_nominative_short,
            long = R.string.length_ft_nominative_long
        ),
        per = UnitTranslation(
            short = R.string.duration_sec_per_short,
            long = R.string.duration_sec_per_long
        ),
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit.div(30.48) },
        convertToReference = { valueInThisUnit -> valueInThisUnit.times(30.48) },
        decimals = UnitDecimals(narrow = 0, short = 1, long = 2),
        chartStep = 15.0
    ),

    /**
     * Not an unit, but a scale, so precision will be lost in the process!
     */
    BEAUFORT_SCALE(
        id = "bf",
        displayName = UnitTranslation(
            short = R.string.speed_bf_display_name_short,
            long = R.string.speed_bf_display_name_long
        ),
        nominative = UnitTranslation(
            short = R.string.speed_bf_nominative_short,
            long = R.string.speed_bf_nominative_long
        ),
        convertFromReference = { valueInDefaultUnit ->
            when (valueInDefaultUnit.toLong()) {
                in 0L..<WIND_SPEED_1 -> 0.0
                in WIND_SPEED_1..<WIND_SPEED_2 -> 1.0
                in WIND_SPEED_2..<WIND_SPEED_3 -> 2.0
                in WIND_SPEED_3..<WIND_SPEED_4 -> 3.0
                in WIND_SPEED_4..<WIND_SPEED_5 -> 4.0
                in WIND_SPEED_5..<WIND_SPEED_6 -> 5.0
                in WIND_SPEED_6..<WIND_SPEED_7 -> 6.0
                in WIND_SPEED_7..<WIND_SPEED_8 -> 7.0
                in WIND_SPEED_8..<WIND_SPEED_9 -> 8.0
                in WIND_SPEED_9..<WIND_SPEED_10 -> 9.0
                in WIND_SPEED_10..<WIND_SPEED_11 -> 10.0
                in WIND_SPEED_11..<WIND_SPEED_12 -> 11.0
                in WIND_SPEED_12..Long.MAX_VALUE -> 12.0
                else -> 0.0
            }
        },
        convertToReference = { valueInThisUnit ->
            when (valueInThisUnit) {
                in 0.0..<1.0 -> 0
                in 1.0..<2.0 -> WIND_SPEED_1
                in 2.0..<3.0 -> WIND_SPEED_2
                in 3.0..<4.0 -> WIND_SPEED_3
                in 4.0..<5.0 -> WIND_SPEED_4
                in 5.0..<6.0 -> WIND_SPEED_5
                in 6.0..<7.0 -> WIND_SPEED_6
                in 7.0..<8.0 -> WIND_SPEED_7
                in 8.0..<9.0 -> WIND_SPEED_8
                in 9.0..<10.0 -> WIND_SPEED_9
                in 10.0..<11.0 -> WIND_SPEED_10
                in 11.0..<12.0 -> WIND_SPEED_11
                in 12.0..Double.MAX_VALUE -> WIND_SPEED_12
                else -> 0
            }.toDouble()
        },
        decimals = UnitDecimals(0),
        chartStep = 2.0
    ),
    ;

    @RequiresApi(Build.VERSION_CODES.N)
    override fun getMeasureUnit(): MeasureUnit? {
        return when (this) {
            CENTIMETER_PER_SECOND -> MeasureUnit.CENTIMETER
            METER_PER_SECOND -> MeasureUnit.METER_PER_SECOND
            KILOMETER_PER_HOUR -> MeasureUnit.KILOMETER_PER_HOUR
            MILE_PER_HOUR -> MeasureUnit.MILE_PER_HOUR
            KNOT -> if (supportsMeasureUnitKnot()) MeasureUnit.KNOT else null
            FOOT_PER_SECOND -> MeasureUnit.FOOT
            BEAUFORT_SCALE -> if (supportsMeasureUnitBeaufort()) MeasureUnit.BEAUFORT else null
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun getPerMeasureUnit(): MeasureUnit? {
        return when (this) {
            CENTIMETER_PER_SECOND -> MeasureUnit.SECOND
            FOOT_PER_SECOND -> MeasureUnit.SECOND
            else -> null
        }
    }

    /**
     * Override to:
     * - Never use ICU with Beaufort scale for Japanese and Traditional Chinese
     * - Use fr_CA ICU unit instead of fr for French
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
        // Translations missing in CLDR for some languages
        // Beaufort:
        // - eo, es, eu, ga: missing
        // - ja: use 風力7 instead of “B 7”
        // - Simplified Chinese: use 7級 rather than “B 7”
        // - Traditional Chinese: use 7級 rather than the verbose “蒲福風級 7 級”
        if ((this == KNOT && locale.language.equals("eo", ignoreCase = true)) ||
            (
                this == BEAUFORT_SCALE &&
                    arrayOf("eo", "es", "eu", "ga", "ja", "zh").any {
                        locale.language.equals(it, ignoreCase = true)
                    }
                )
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

        val correctedLocale = if (supportsMeasureUnitBeaufort() && this == BEAUFORT_SCALE) {
            if (locale.language.equals("fr", ignoreCase = true)) {
                // fr uses the incorrect unit (it should be "%s Bf" instead of "%s Bft"), replace with fr_CA
                Locale.Builder().setLanguage("fr").setRegion("CA").build()
            } else if (locale.language.equals("de", ignoreCase = true)) {
                // de uses the incorrect unit (it should be "%s Bft" instead of "B %s"), replace with de_CH
                Locale.Builder().setLanguage("de").setRegion("CH").build()
            } else {
                locale
            }
        } else {
            /**
             * Taiwan guidelines: https://www.bsmi.gov.tw/wSite/public/Attachment/f1736149048776.pdf
             * Ongoing issue: https://unicode-org.atlassian.net/jira/software/c/projects/CLDR/issues/CLDR-10604
             */
            if (locale.language.equals("zh", ignoreCase = true) &&
                arrayOf("TW", "HK", "MO").any { c -> locale.country.equals(c, ignoreCase = true) } &&
                unitWidth != UnitWidth.LONG
            ) {
                Locale.Builder().setLanguage("en").setRegion("001").build()
            } else {
                locale
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

        // In centimeters per second
        const val WIND_SPEED_1 = 30
        const val WIND_SPEED_2 = 160
        const val WIND_SPEED_3 = 340
        const val WIND_SPEED_4 = 550
        const val WIND_SPEED_5 = 800
        const val WIND_SPEED_6 = 1080
        const val WIND_SPEED_7 = 1390
        const val WIND_SPEED_8 = 1720
        const val WIND_SPEED_9 = 2080
        const val WIND_SPEED_10 = 2450
        const val WIND_SPEED_11 = 2850
        const val WIND_SPEED_12 = 3270

        // Extended scale, not official but may be used in the daily chart in the future, just in case
        const val WIND_SPEED_13 = 3700
        const val WIND_SPEED_14 = 4150
        const val WIND_SPEED_15 = 4620
        const val WIND_SPEED_16 = 5100
        const val WIND_SPEED_17 = 5610

        /**
         * Copyright © 1991-Present Unicode, Inc.
         * License: Unicode License v3 https://www.unicode.org/license.txt
         * Source: https://github.com/unicode-org/cldr/blob/3f3967f3cbadc56bbb44a9aed20784e82ac64c67/common/supplemental/units.xml#L570-L574
         */
        fun getDefaultUnit(
            locale: Locale = Locale.getDefault(),
        ) = when (locale.country) {
            "CN", "DK", "FI", "JP", "KR", "NO", "PL", "RU", "SE" -> METER_PER_SECOND
            "GB", "US" -> MILE_PER_HOUR
            else -> KILOMETER_PER_HOUR
        }

        fun getUnit(id: String): SpeedUnit? {
            return entries.firstOrNull { it.id == id }
        }
    }
}

/** Converts the given time speed [value] expressed in the specified [sourceUnit] into the specified [targetUnit]. */
internal fun convertSpeedUnit(
    value: Double,
    sourceUnit: SpeedUnit,
    targetUnit: SpeedUnit,
): Double {
    return if (sourceUnit == SpeedUnit.CENTIMETER_PER_SECOND) {
        targetUnit.convertFromReference(value)
    } else if (targetUnit == SpeedUnit.CENTIMETER_PER_SECOND) {
        sourceUnit.convertToReference(value)
    } else {
        targetUnit.convertFromReference(sourceUnit.convertToReference(value))
    }
}
