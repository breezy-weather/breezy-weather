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

package org.breezyweather.unit.duration

import android.content.Context
import android.icu.number.NumberFormatter
import android.icu.text.MeasureFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import android.os.Build
import androidx.annotation.RequiresApi
import org.breezyweather.unit.R
import org.breezyweather.unit.formatting.UnitWidth
import org.breezyweather.unit.formatting.format
import org.breezyweather.unit.formatting.formatWithMeasureFormat
import org.breezyweather.unit.formatting.formatWithNumberFormatter
import org.breezyweather.unit.supportsMeasureFormat
import org.breezyweather.unit.supportsNumberFormatter
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

/**
 * Duration already exists in Kotlin.time
 * Extending it with what we need
 */

/**
 * Format a value for the following parameters:
 * @param unit the unit the weather value must be converted to
 * @param width this will determine how many decimals will be displayed based on the unit definition
 * @param locale the locale for the formatting, or the default system locale
 * @param useNumberFormatter true if [NumberFormatter] should be used on compatible Android devices
 * @param useMeasureFormat true if [MeasureFormat] should be used on compatible Android devices
 */
fun Duration.formatValue(
    unit: DurationUnit,
    width: UnitWidth = UnitWidth.SHORT,
    locale: Locale = Locale.getDefault(),
    useNumberFormatter: Boolean = true,
    useMeasureFormat: Boolean = true,
): String {
    return toDouble(unit).format(
        decimals = unit.getPrecision(width),
        locale = locale,
        useNumberFormatter = useNumberFormatter,
        useNumberFormat = useMeasureFormat
    )
}

/**
 * Allows to format a duration for the following parameters:
 * @param context Context in case we need to use Android translation strings
 * @param unit the unit the duration value must be converted to
 * @param smallestUnit if lower than unit, will use multiple components, such as "1 hr and 30 min", instead of “1.5 hr”
 * @param valueWidth this will determine how many decimals will be displayed based on the unit definition
 * @param unitWidth this will make the unit more or less short
 * @param locale the locale for the formatting, or the default system locale
 * @param useNumberFormatter true if [NumberFormatter] should be used on compatible Android devices
 * @param useMeasureFormat true if [MeasureFormat] should be used on compatible Android devices
 */
fun Duration.format(
    context: Context,
    unit: DurationUnit,
    smallestUnit: DurationUnit = unit,
    valueWidth: UnitWidth = UnitWidth.SHORT,
    unitWidth: UnitWidth = UnitWidth.SHORT,
    locale: Locale = Locale.getDefault(),
    useNumberFormatter: Boolean = true,
    useMeasureFormat: Boolean = true,
): String {
    if (supportsMeasureFormat() && (useNumberFormatter || useMeasureFormat)) {
        // LogHelper.log(msg = "Formatting with ICU ${enum.id}: ${enum.measureUnit} per ${enum.perMeasureUnit}")

        return if (supportsNumberFormatter() &&
            useNumberFormatter &&
            unit == smallestUnit // NumberFormatter only supports one unit at a time
        ) {
            val convertedValue = toDouble(unit)
            unit.toMeasureUnit().formatWithNumberFormatter(
                locale = locale,
                value = convertedValue,
                precision = unit.getPrecision(valueWidth),
                numberFormatterWidth = unitWidth.numberFormatterWidth!!
            )
        } else {
            if (unit == smallestUnit) {
                val convertedValue = toDouble(unit)
                unit.toMeasureUnit().formatWithMeasureFormat(
                    locale = locale,
                    value = convertedValue,
                    precision = unit.getPrecision(valueWidth),
                    measureFormatWidth = unitWidth.measureFormatWidth!!
                )
            } else {
                formatMultipleDurationsWithMeasureFormat(
                    unit = unit,
                    smallestUnit = smallestUnit,
                    locale = locale,
                    measureFormatWidth = unitWidth.measureFormatWidth!!
                )
            }
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
 * Format duration into multiple components like:
 * 1 hr, 30 min and 24 sec
 *
 * It is very simplified for our usage, as it only supports hours, minutes and seconds
 * It also doesn’t support decimals
 */
@RequiresApi(api = Build.VERSION_CODES.N)
private fun Duration.formatMultipleDurationsWithMeasureFormat(
    unit: DurationUnit,
    smallestUnit: DurationUnit,
    locale: Locale = Locale.getDefault(),
    measureFormatWidth: MeasureFormat.FormatWidth,
): String {
    val measureFormat = MeasureFormat.getInstance(locale, measureFormatWidth)

    return toComponents { hours, minutes, seconds, _ ->
        val measures = mutableListOf<Measure>()

        val hasHours = hours != 0L
        val hasMinutes = minutes != 0
        val hasSeconds = seconds != 0

        if (hasHours) {
            measures.add(Measure(hours, MeasureUnit.HOUR))
        }

        if (hasMinutes && smallestUnit <= DurationUnit.MINUTES) {
            measures.add(Measure(minutes, MeasureUnit.MINUTE))
        }

        if (hasSeconds && smallestUnit <= DurationUnit.SECONDS) {
            measures.add(Measure(seconds, MeasureUnit.SECOND))
        }

        if (measures.isEmpty()) {
            measures.add(Measure(0, unit.toMeasureUnit()))
        }

        measureFormat.formatMeasures(*measures.toTypedArray())
    }
}

/**
 * Format a value and its unit using Android translations for the following parameters:
 * @param context Context to get Android translation strings
 * @param unit the unit the weather value must be converted to
 * @param smallestUnit if lower than unit, will use multiple components, such as "1 hr, 30 min", instead of “1.5 hr”
 * @param valueWidth this will determine how many decimals will be displayed based on the unit definition
 * @param unitWidth this will make the unit more or less short
 * @param locale the locale for the formatting, or the default system locale
 * @param useNumberFormatter true if [NumberFormatter] should be used for value formatting on compatible devices
 * @param useMeasureFormat true if [MeasureFormat] should be used for value formatting on compatible Android devices
 */
fun Duration.formatWithAndroidTranslations(
    context: Context,
    unit: DurationUnit,
    smallestUnit: DurationUnit = unit,
    valueWidth: UnitWidth = UnitWidth.SHORT,
    unitWidth: UnitWidth = UnitWidth.SHORT,
    locale: Locale = Locale.getDefault(),
    useNumberFormatter: Boolean = true,
    useMeasureFormat: Boolean = true,
): String {
    return if (unit == smallestUnit) {
        context.getString(
            unit.getNominative().let {
                when (unitWidth) {
                    UnitWidth.SHORT -> it.short
                    UnitWidth.LONG -> it.long
                    UnitWidth.NARROW -> it.narrow
                }
            },
            formatValue(
                unit = unit,
                width = valueWidth,
                locale = locale,
                useNumberFormatter = useNumberFormatter,
                useMeasureFormat = useMeasureFormat
            )
        )
    } else {
        formatMultipleDurationsWithAndroidTranslations(
            context = context,
            locale = locale,
            width = unitWidth,
            unit = unit,
            smallestUnit = smallestUnit,
            useNumberFormatter = useNumberFormatter,
            useMeasureFormat = useMeasureFormat
        )
    }
}

/**
 * Format duration into multiple components like:
 * 1 hr, 30 min, 24 sec
 *
 * It is very simplified for our usage, as it only supports hours, minutes and seconds
 * It also doesn’t support decimals
 */
private fun Duration.formatMultipleDurationsWithAndroidTranslations(
    context: Context,
    locale: Locale = Locale.getDefault(),
    width: UnitWidth = UnitWidth.SHORT,
    unit: DurationUnit,
    smallestUnit: DurationUnit,
    useNumberFormatter: Boolean = true,
    useMeasureFormat: Boolean = true,
): String {
    return toComponents { hours, minutes, seconds, _ ->
        val measures = mutableListOf<String>()

        val hasHours = hours != 0L
        val hasMinutes = minutes != 0
        val hasSeconds = seconds != 0

        if (hasHours) {
            measures.add(
                hours.hours.formatWithAndroidTranslations(
                    context = context,
                    unit = DurationUnit.HOURS,
                    smallestUnit = DurationUnit.HOURS,
                    valueWidth = UnitWidth.NARROW,
                    unitWidth = width,
                    locale = locale,
                    useNumberFormatter = useNumberFormatter,
                    useMeasureFormat = useMeasureFormat
                )
            )
        }

        if (hasMinutes && smallestUnit <= DurationUnit.MINUTES) {
            measures.add(
                minutes.minutes.formatWithAndroidTranslations(
                    context = context,
                    unit = DurationUnit.MINUTES,
                    smallestUnit = DurationUnit.MINUTES,
                    valueWidth = UnitWidth.NARROW,
                    unitWidth = width,
                    locale = locale,
                    useNumberFormatter = useNumberFormatter,
                    useMeasureFormat = useMeasureFormat
                )
            )
        }

        if (hasSeconds && smallestUnit <= DurationUnit.SECONDS) {
            measures.add(
                seconds.seconds.formatWithAndroidTranslations(
                    context = context,
                    unit = DurationUnit.SECONDS,
                    smallestUnit = DurationUnit.SECONDS,
                    valueWidth = UnitWidth.NARROW,
                    unitWidth = width,
                    locale = locale,
                    useNumberFormatter = useNumberFormatter,
                    useMeasureFormat = useMeasureFormat
                )
            )
        }

        if (measures.isEmpty()) {
            measures.add(
                0.seconds.formatWithAndroidTranslations(
                    context = context,
                    unit = unit,
                    smallestUnit = unit,
                    valueWidth = UnitWidth.NARROW,
                    unitWidth = width,
                    locale = locale,
                    useNumberFormatter = useNumberFormatter,
                    useMeasureFormat = useMeasureFormat
                )
            )
        }

        measures.joinToString(context.getString(R.string.locale_separator))
    }
}

fun Duration.toValidDailyOrNull(): Duration? {
    return if (toDouble(DurationUnit.HOURS) <= 24.0) this else null
}

fun Duration.toValidHalfDayOrNull(): Duration? {
    return if (toDouble(DurationUnit.HOURS) <= 12.0) this else null
}
