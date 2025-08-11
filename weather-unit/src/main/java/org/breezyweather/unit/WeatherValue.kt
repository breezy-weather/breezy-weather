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
import android.icu.number.LocalizedNumberFormatter
import android.icu.number.NumberFormatter
import android.icu.number.Precision
import android.icu.text.MeasureFormat
import android.icu.text.NumberFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import android.icu.util.TimeUnit
import android.os.Build
import androidx.annotation.RequiresApi
import org.breezyweather.unit.formatting.UnitWidth
import org.breezyweather.unit.formatting.format
import java.text.FieldPosition
import java.util.Locale

interface WeatherValue<T : WeatherUnit> {

    fun toDouble(unit: T): Double

    /**
     * Format a value for the following parameters:
     * @param unit the unit the weather value must be converted to
     * @param width this will determine how many decimals will be displayed based on the unit definition
     * @param locale the locale for the formatting, or the default system locale
     * @param useNumberFormatter true if [NumberFormatter] should be used on compatible Android devices
     * @param useMeasureFormat true if [MeasureFormat] should be used on compatible Android devices
     */
    fun formatValue(
        unit: T,
        width: UnitWidth = UnitWidth.SHORT,
        locale: Locale = Locale.getDefault(),
        useNumberFormatter: Boolean = true,
        useMeasureFormat: Boolean = true,
    ): String {
        return toDouble(unit).format(
            decimals = when (width) {
                UnitWidth.SHORT -> unit.decimals.short
                UnitWidth.NARROW -> unit.decimals.narrow
                UnitWidth.LONG -> unit.decimals.long
            },
            locale = locale,
            useNumberFormatter = useNumberFormatter,
            useMeasureFormat = useMeasureFormat
        )
    }

    /**
     * Format a value and its unit for the following parameters:
     * @param context Context in case we need to use Android translation strings
     * @param unit the unit the weather value must be converted to
     * @param valueWidth this will determine how many decimals will be displayed based on the unit definition
     * @param unitWidth this will make the unit more or less short
     * @param locale the locale for the formatting, or the default system locale
     * @param useNumberFormatter true if [NumberFormatter] should be used on compatible Android devices
     * @param useMeasureFormat true if [MeasureFormat] should be used on compatible Android devices
     */
    fun format(
        context: Context,
        unit: T,
        valueWidth: UnitWidth = UnitWidth.SHORT,
        unitWidth: UnitWidth = UnitWidth.SHORT,
        locale: Locale = Locale.getDefault(),
        useNumberFormatter: Boolean = true,
        useMeasureFormat: Boolean = true,
    ): String {
        if (unit.measureUnit != null &&
            (unit.perMeasureUnit == null || Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) &&
            (useNumberFormatter || useMeasureFormat)
        ) {
            val convertedValue = toDouble(unit)
            // LogHelper.log(msg = "Formatting with ICU ${enum.id}: ${enum.measureUnit} per ${enum.perMeasureUnit}")

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && useNumberFormatter) {
                formatWithNumberFormatter(
                    locale = locale,
                    value = convertedValue,
                    unit = unit.measureUnit!!,
                    perUnit = unit.perMeasureUnit,
                    precision = when (valueWidth) {
                        UnitWidth.SHORT -> unit.decimals.short
                        UnitWidth.NARROW -> unit.decimals.narrow
                        UnitWidth.LONG -> unit.decimals.long
                    },
                    numberFormatterWidth = unitWidth.numberFormatterWidth!!
                )
            } else {
                formatWithMeasureFormat(
                    locale = locale,
                    value = convertedValue,
                    unit = unit.measureUnit!!,
                    perUnit = unit.perMeasureUnit,
                    precision = when (valueWidth) {
                        UnitWidth.SHORT -> unit.decimals.short
                        UnitWidth.NARROW -> unit.decimals.narrow
                        UnitWidth.LONG -> unit.decimals.long
                    },
                    measureFormatWidth = unitWidth.measureFormatWidth!!
                )
            }
        }

        // LogHelper.log(msg = "Not formatting with ICU ${enum.id} in ${context.currentLocale}")
        return formatWithAndroidTranslations(
            context = context,
            unit = unit,
            valueWidth = valueWidth,
            unitWidth = unitWidth,
            locale = locale,
            useNumberFormatter = useNumberFormatter,
            useMeasureFormat = useMeasureFormat
        )
    }

    /**
     * Format a value and its unit using Android translations for the following parameters:
     * @param context Context to get Android translation strings
     * @param unit the unit the weather value must be converted to
     * @param valueWidth this will determine how many decimals will be displayed based on the unit definition
     * @param unitWidth this will make the unit more or less short
     * @param locale the locale for the formatting, or the default system locale
     * @param useNumberFormatter true if [NumberFormatter] should be used for value formatting on compatible devices
     * @param useMeasureFormat true if [MeasureFormat] should be used for value formatting on compatible Android devices
     */
    fun formatWithAndroidTranslations(
        context: Context,
        unit: T,
        valueWidth: UnitWidth = UnitWidth.SHORT,
        unitWidth: UnitWidth = UnitWidth.SHORT,
        locale: Locale = Locale.getDefault(),
        useNumberFormatter: Boolean = true,
        useMeasureFormat: Boolean = true,
    ): String {
        return context.getString(
            when (unitWidth) {
                UnitWidth.SHORT -> unit.nominative.short
                UnitWidth.LONG -> unit.nominative.long
                UnitWidth.NARROW -> unit.nominative.narrow
            },
            formatValue(
                unit = unit,
                width = valueWidth,
                locale = locale,
                useNumberFormatter = useNumberFormatter,
                useMeasureFormat = useMeasureFormat
            )
        )
    }

    /**
     * @param locale
     * @param value
     * @param unit
     * @param perUnit an optional per unit
     * @param numberFormatterWidth
     */
    @RequiresApi(api = Build.VERSION_CODES.R)
    fun formatWithNumberFormatter(
        locale: Locale,
        value: Number,
        unit: MeasureUnit,
        perUnit: MeasureUnit? = null,
        precision: Int,
        numberFormatterWidth: NumberFormatter.UnitWidth,
    ): String {
        return (NumberFormatter.withLocale(locale) as LocalizedNumberFormatter)
            .precision(if (precision == 0) Precision.integer() else Precision.maxFraction(precision))
            .unit(unit)
            .perUnit(perUnit)
            .unitWidth(numberFormatterWidth)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    usage(if (unit is TimeUnit) "duration" else null)
                }
            }
            .format(value)
            .toString()
    }

    /**
     * @param locale
     * @param value
     * @param unit
     * @param perUnit an optional per unit. /!\ Only supported on Android SDK >= 26
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    fun formatWithMeasureFormat(
        locale: Locale,
        value: Number,
        unit: MeasureUnit,
        perUnit: MeasureUnit? = null,
        precision: Int,
        measureFormatWidth: MeasureFormat.FormatWidth,
    ): String {
        if (perUnit != null && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            throw UnsupportedOperationException()
        }

        return MeasureFormat
            .getInstance(
                locale,
                measureFormatWidth,
                NumberFormat.getInstance().apply { maximumFractionDigits = precision }
            )
            .let {
                if (perUnit != null) {
                    it.formatMeasurePerUnit(
                        Measure(value, unit),
                        perUnit,
                        StringBuilder(),
                        FieldPosition(0)
                    ).toString()
                } else {
                    it.format(
                        Measure(value, unit)
                    )
                }
            }
    }
}
