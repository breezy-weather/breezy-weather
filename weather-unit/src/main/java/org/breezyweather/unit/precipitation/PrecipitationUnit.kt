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

import android.content.Context
import android.icu.util.MeasureUnit
import android.os.Build
import androidx.annotation.RequiresApi
import org.breezyweather.unit.R
import org.breezyweather.unit.WeatherUnit
import org.breezyweather.unit.formatting.UnitDecimals
import org.breezyweather.unit.formatting.UnitTranslation
import org.breezyweather.unit.formatting.UnitWidth
import org.breezyweather.unit.formatting.formatWithMeasureFormat
import org.breezyweather.unit.formatting.formatWithNumberFormatter
import org.breezyweather.unit.supportsMeasureFormat
import org.breezyweather.unit.supportsMeasureFormatPerUnit
import org.breezyweather.unit.supportsNumberFormatter
import java.util.Locale

enum class PrecipitationUnit(
    override val id: String,
    override val displayName: UnitTranslation,
    override val nominative: UnitTranslation,
    override val per: UnitTranslation? = null,
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
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit },
        convertToReference = { valueInThisUnit -> valueInThisUnit },
        decimals = UnitDecimals(short = 0, long = 1), // Used only by PM2.5 formatting
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
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit.div(1000.0) },
        convertToReference = { valueInThisUnit -> valueInThisUnit.times(1000.0) },
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
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit.div(10000.0) },
        convertToReference = { valueInThisUnit -> valueInThisUnit.times(10000.0) },
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
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit.div(25400.0) },
        convertToReference = { valueInThisUnit -> valueInThisUnit.times(25400.0) },
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
            short = R.string.area_m2_per_short,
            long = R.string.area_m2_per_long
        ),
        convertFromReference = { valueInDefaultUnit -> valueInDefaultUnit.div(1000.0) },
        convertToReference = { valueInThisUnit -> valueInThisUnit.times(1000.0) },
        decimals = UnitDecimals(narrow = 0, short = 1, long = 2),
        chartStep = 5.0
    ),
    ;

    @RequiresApi(Build.VERSION_CODES.N)
    override fun getMeasureUnit(): MeasureUnit? {
        return when (this) {
            MICROMETER -> MeasureUnit.MICROMETER
            MILLIMETER -> MeasureUnit.MILLIMETER
            CENTIMETER -> MeasureUnit.CENTIMETER
            INCH -> MeasureUnit.INCH
            LITER_PER_SQUARE_METER -> MeasureUnit.LITER
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun getPerMeasureUnit(): MeasureUnit? {
        return when (this) {
            LITER_PER_SQUARE_METER -> MeasureUnit.SQUARE_METER
            else -> null
        }
    }

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
        if (locale.language.equals("eo", ignoreCase = true) && this == LITER_PER_SQUARE_METER) {
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

    /**
     * Special case of formatting:
     * mm/h instead of just mm
     */
    fun formatIntensity(
        context: Context,
        value: Number,
        valueWidth: UnitWidth = UnitWidth.SHORT,
        unitWidth: UnitWidth = UnitWidth.SHORT,
        locale: Locale = Locale.getDefault(),
        useNumberFormatter: Boolean = true,
        useMeasureFormat: Boolean = true,
    ): String {
        if (supportsMeasureFormat() &&
            getMeasureUnit() != null &&
            getPerMeasureUnit() == null && // Liter per square meter would have 2 “per”, so not supported!
            supportsMeasureFormatPerUnit() &&
            (useNumberFormatter || useMeasureFormat)
        ) {
            val correctedLocale = locale.let {
                /*
                 * Use English units with Traditional Chinese
                 *
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

            return if (supportsNumberFormatter() && useNumberFormatter) {
                getMeasureUnit()!!.formatWithNumberFormatter(
                    locale = correctedLocale,
                    value = value,
                    perUnit = MeasureUnit.HOUR,
                    precision = getPrecision(valueWidth),
                    numberFormatterWidth = unitWidth.numberFormatterWidth!!
                )
            } else {
                getMeasureUnit()!!.formatWithMeasureFormat(
                    locale = correctedLocale,
                    value = value,
                    perUnit = MeasureUnit.HOUR,
                    precision = getPrecision(valueWidth),
                    measureFormatWidth = unitWidth.measureFormatWidth!!
                )
            }
        }

        // LogHelper.log(msg = "Not formatting with ICU ${enum.id} in ${context.currentLocale}")
        return formatIntensityWithAndroidTranslations(
            context = context,
            value = value,
            valueWidth = valueWidth,
            unitWidth = unitWidth,
            locale = locale,
            useNumberFormatter = useNumberFormatter,
            useMeasureFormat = useMeasureFormat
        )
    }

    fun formatIntensityWithAndroidTranslations(
        context: Context,
        value: Number,
        valueWidth: UnitWidth = UnitWidth.SHORT,
        unitWidth: UnitWidth = UnitWidth.SHORT,
        locale: Locale = Locale.getDefault(),
        useNumberFormatter: Boolean = true,
        useMeasureFormat: Boolean = true,
    ): String {
        return context.getString(
            when (unitWidth) {
                UnitWidth.SHORT -> R.string.duration_hr_per_short
                UnitWidth.LONG -> R.string.duration_hr_per_long
                UnitWidth.NARROW -> R.string.duration_hr_per_short
            },
            formatWithAndroidTranslations(
                context = context,
                value = value,
                valueWidth = valueWidth,
                unitWidth = unitWidth,
                locale = locale,
                useNumberFormatter = useNumberFormatter,
                useMeasureFormat = useMeasureFormat
            )
        )
    }

    companion object {

        /**
         * Copyright © 1991-Present Unicode, Inc.
         * License: Unicode License v3 https://www.unicode.org/license.txt
         * Source (simplified): https://github.com/unicode-org/cldr/blob/3f3967f3cbadc56bbb44a9aed20784e82ac64c67/common/supplemental/units.xml#L474-L478
         */
        fun getDefaultUnit(
            locale: Locale = Locale.getDefault(),
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
