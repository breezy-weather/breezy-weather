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

package org.breezyweather.unit

import android.content.Context
import android.icu.text.MeasureFormat
import android.icu.util.MeasureUnit
import android.os.Build
import org.breezyweather.unit.formatting.UnitDecimals
import org.breezyweather.unit.formatting.UnitTranslation
import org.breezyweather.unit.formatting.UnitWidth
import org.breezyweather.unit.formatting.format
import org.breezyweather.unit.formatting.formatWithMeasureFormat
import org.breezyweather.unit.formatting.formatWithNumberFormatter
import java.util.Locale

interface WeatherUnit {
    /**
     * An internal identifier for the unit
     */
    val id: String

    /**
     * Name of the unit when used standalone (without value)
     */
    val displayName: UnitTranslation

    /**
     * String formatters for quantity of the unit
     */
    val nominative: UnitTranslation

    /**
     * Optional string formatter for “per”. For example /m³
     */
    val per: UnitTranslation?

    /**
     * [MeasureUnit] used with ICU formatting for compatible Android devices
     */
    val measureUnit: MeasureUnit?

    /**
     * [MeasureUnit] used with ICU formatting for compatible Android devices
     */
    val perMeasureUnit: MeasureUnit?

    /**
     * How many decimals should be used when displaying a number in this unit
     * Must be positive
     */
    val decimals: UnitDecimals

    fun getDisplayName(
        context: Context,
        locale: Locale = Locale.getDefault(),
        width: UnitWidth = UnitWidth.SHORT,
        useMeasureFormat: Boolean = true,
    ): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
            useMeasureFormat &&
            measureUnit != null
        ) {
            if (perMeasureUnit == null) {
                return MeasureFormat
                    .getInstance(locale, width.measureFormatWidth)
                    .getUnitDisplayName(measureUnit)
            } else if (perMeasureUnit != null && per != null) {
                return context.getString(
                    when (width) {
                        UnitWidth.SHORT -> per!!.short
                        UnitWidth.LONG -> per!!.long
                        UnitWidth.NARROW -> per!!.narrow
                    },
                    MeasureFormat
                        .getInstance(locale, width.measureFormatWidth)
                        .getUnitDisplayName(measureUnit)
                )
            }
        }

        val formattingWithoutPer = context.getString(
            when (width) {
                UnitWidth.SHORT -> displayName.short
                UnitWidth.LONG -> displayName.long
                UnitWidth.NARROW -> displayName.narrow
            }
        )

        return per?.let {
            context.getString(
                when (width) {
                    UnitWidth.SHORT -> it.short
                    UnitWidth.LONG -> it.long
                    UnitWidth.NARROW -> it.narrow
                },
                formattingWithoutPer
            )
        } ?: formattingWithoutPer
    }

    /**
     * TODO: This may produce discrepancy compared to the actual nominative unit displayed by MeasureFormat or
     *  NumberFormatter
     *  Use fr_CA as your language to see the difference of “po Hg” being displayed “inHg” (because inheriting from
     *  French from France Android translation)
     */
    fun getNominativeUnit(
        context: Context,
        width: UnitWidth = UnitWidth.SHORT,
        useNumberFormatter: Boolean = true,
        useMeasureFormat: Boolean = true,
    ): String {
        return context.getString(
            when (width) {
                UnitWidth.SHORT -> nominative.short
                UnitWidth.LONG -> nominative.long
                UnitWidth.NARROW -> nominative.narrow
            },
            ""
        ).trim()
    }

    fun getPrecision(width: UnitWidth): Int = when (width) {
        UnitWidth.SHORT -> decimals.short
        UnitWidth.NARROW -> decimals.narrow
        UnitWidth.LONG -> decimals.long
    }

    fun format(
        context: Context,
        value: Number,
        valueWidth: UnitWidth = UnitWidth.SHORT,
        unitWidth: UnitWidth = UnitWidth.SHORT,
        locale: Locale = Locale.getDefault(),
        showSign: Boolean = false,
        useNumberFormatter: Boolean = true,
        useMeasureFormat: Boolean = true,
    ): String {
        if (measureUnit != null &&
            (perMeasureUnit == null || Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) &&
            (useNumberFormatter || useMeasureFormat)
        ) {
            // LogHelper.log(msg = "Formatting with ICU ${enum.id}: ${enum.measureUnit} per ${enum.perMeasureUnit}")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && useNumberFormatter) {
                return measureUnit!!.formatWithNumberFormatter(
                    locale = locale,
                    value = value,
                    perUnit = perMeasureUnit,
                    precision = getPrecision(valueWidth),
                    numberFormatterWidth = unitWidth.numberFormatterWidth!!,
                    showSign = showSign
                )
            }

            if (!showSign) {
                return measureUnit!!.formatWithMeasureFormat(
                    locale = locale,
                    value = value,
                    perUnit = perMeasureUnit,
                    precision = getPrecision(valueWidth),
                    measureFormatWidth = unitWidth.measureFormatWidth!!
                )
            }
        }

        // LogHelper.log(msg = "Not formatting with ICU ${enum.id} in ${context.currentLocale}")
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

    fun formatWithAndroidTranslations(
        context: Context,
        value: Number,
        valueWidth: UnitWidth = UnitWidth.SHORT,
        unitWidth: UnitWidth = UnitWidth.SHORT,
        locale: Locale = Locale.getDefault(),
        showSign: Boolean = false,
        useNumberFormatter: Boolean = true,
        useMeasureFormat: Boolean = true,
    ): String {
        val formattingWithoutPer = context.getString(
            when (unitWidth) {
                UnitWidth.SHORT -> nominative.short
                UnitWidth.LONG -> nominative.long
                UnitWidth.NARROW -> nominative.narrow
            },
            value.format(
                decimals = getPrecision(valueWidth),
                locale = locale,
                showSign = showSign,
                useNumberFormatter = useNumberFormatter,
                useNumberFormat = useMeasureFormat
            )
        )

        return per?.let {
            context.getString(
                when (unitWidth) {
                    UnitWidth.SHORT -> it.short
                    UnitWidth.LONG -> it.long
                    UnitWidth.NARROW -> it.narrow
                },
                formattingWithoutPer
            )
        } ?: formattingWithoutPer
    }
}
