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
import android.icu.number.NumberFormatter
import android.icu.text.MeasureFormat
import android.os.Build
import org.breezyweather.unit.formatting.UnitWidth
import org.breezyweather.unit.formatting.format
import org.breezyweather.unit.formatting.formatWithMeasureFormat
import org.breezyweather.unit.formatting.formatWithNumberFormatter
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
            decimals = unit.getPrecision(width),
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
                unit.measureUnit!!.formatWithNumberFormatter(
                    locale = locale,
                    value = convertedValue,
                    perUnit = unit.perMeasureUnit,
                    precision = unit.getPrecision(valueWidth),
                    numberFormatterWidth = unitWidth.numberFormatterWidth!!
                )
            } else {
                unit.measureUnit!!.formatWithMeasureFormat(
                    locale = locale,
                    value = convertedValue,
                    perUnit = unit.perMeasureUnit,
                    precision = unit.getPrecision(valueWidth),
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
}
