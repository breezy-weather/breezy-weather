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

package org.breezyweather.common.extensions

import android.content.Context
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.unit.duration.format
import org.breezyweather.unit.formatting.UnitWidth
import org.breezyweather.unit.pressure.Pressure
import kotlin.time.Duration
import kotlin.time.DurationUnit

/**
 * Convenient format function with parameters filled for our app
 */
fun Pressure.formatMeasure(
    context: Context,
    valueWidth: UnitWidth = UnitWidth.SHORT,
    unitWidth: UnitWidth = UnitWidth.SHORT,
): String {
    val settings = SettingsManager.getInstance(context)
    return format(
        context = context,
        unit = settings.getPressureUnit(context),
        valueWidth = valueWidth,
        unitWidth = unitWidth,
        locale = context.currentLocale,
        useNumberFormatter = settings.useNumberFormatter,
        useMeasureFormat = settings.useMeasureFormat
    )
}

/**
 * Convenient format function with parameters filled for our app
 */
fun Pressure.formatValue(
    context: Context,
    width: UnitWidth = UnitWidth.SHORT,
): String {
    val settings = SettingsManager.getInstance(context)
    return formatValue(
        unit = settings.getPressureUnit(context),
        width = width,
        locale = context.currentLocale,
        useNumberFormatter = settings.useNumberFormatter,
        useMeasureFormat = settings.useMeasureFormat
    )
}

/**
 * Convenient format function with parameters filled for our app
 */
fun Duration.formatTime(
    context: Context,
    smallestUnit: DurationUnit = DurationUnit.HOURS,
    valueWidth: UnitWidth = UnitWidth.SHORT,
    unitWidth: UnitWidth = UnitWidth.SHORT,
): String {
    val settings = SettingsManager.getInstance(context)
    return format(
        context = context,
        unit = DurationUnit.HOURS,
        smallestUnit = smallestUnit,
        valueWidth = valueWidth,
        unitWidth = unitWidth,
        locale = context.currentLocale,
        useNumberFormatter = settings.useNumberFormatter,
        useMeasureFormat = settings.useMeasureFormat
    )
}
