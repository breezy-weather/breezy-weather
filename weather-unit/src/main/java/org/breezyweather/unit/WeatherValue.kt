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
import org.breezyweather.unit.formatting.UnitWidth
import org.breezyweather.unit.formatting.format
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
        showSign: Boolean = false,
        useNumberFormatter: Boolean = true,
        useMeasureFormat: Boolean = true,
    ): String {
        return unit.format(
            context = context,
            value = toDouble(unit),
            valueWidth = valueWidth,
            unitWidth = unitWidth,
            locale = locale,
            showSign = showSign,
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
        value: Number,
        valueWidth: UnitWidth = UnitWidth.SHORT,
        unitWidth: UnitWidth = UnitWidth.SHORT,
        locale: Locale = Locale.getDefault(),
        useNumberFormatter: Boolean = true,
        useMeasureFormat: Boolean = true,
    ): String {
        return unit.formatWithAndroidTranslations(
            context = context,
            value = value,
            valueWidth = valueWidth,
            unitWidth = unitWidth,
            locale = locale,
            useNumberFormatter = useNumberFormatter,
            useMeasureFormat = useMeasureFormat
        )
    }
}
