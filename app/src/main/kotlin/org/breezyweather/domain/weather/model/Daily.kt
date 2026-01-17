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

package org.breezyweather.domain.weather.model

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Daily
import org.breezyweather.R
import org.breezyweather.common.extensions.capitalize
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.formatMeasure
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.getLongWeekdayDayMonth
import org.breezyweather.common.extensions.getWeek
import org.breezyweather.unit.temperature.TemperatureUnit
import java.util.Calendar
import kotlin.time.Duration.Companion.days

/**
 * Shows one of the following valid label:
 * - Yesterday
 * - Today
 * - Tomorrow
 * - Monday, Tuesday, etc
 * - Monday DD MMMM, Tuesday DD MMMM, etc
 */
fun Daily.getFullLabel(location: Location, context: Context): String {
    val current = Calendar.getInstance(location.timeZone)

    // In more than a week? Show full weekday + day + month
    if ((date.time > current.time.time.plus(6.days.inWholeMilliseconds))) {
        return date.getFormattedDate(getLongWeekdayDayMonth(context), location, context)
            .capitalize(context.currentLocale)
    }

    val thisDay = Calendar.getInstance(location.timeZone)
    thisDay.time = date

    return if (current[Calendar.YEAR] == thisDay[Calendar.YEAR] &&
        current[Calendar.DAY_OF_YEAR] == thisDay[Calendar.DAY_OF_YEAR]
    ) {
        context.getString(R.string.daily_today)
    } else if (
        (
            current[Calendar.YEAR] == thisDay[Calendar.YEAR] &&
                current[Calendar.DAY_OF_YEAR] - 1 == thisDay[Calendar.DAY_OF_YEAR]
            ) ||
        ( // Special new year case
            (current[Calendar.YEAR] - 1 == thisDay[Calendar.YEAR]) &&
                current[Calendar.DAY_OF_YEAR] == 1 &&
                thisDay[Calendar.DAY_OF_YEAR] in 365..366
            )
    ) {
        context.getString(R.string.daily_yesterday)
    } else if (
        (
            current[Calendar.YEAR] == thisDay[Calendar.YEAR] &&
                current[Calendar.DAY_OF_YEAR] + 1 == thisDay[Calendar.DAY_OF_YEAR]
            ) ||
        ( // Special new year case
            (current[Calendar.YEAR] + 1 == thisDay[Calendar.YEAR]) &&
                thisDay[Calendar.DAY_OF_YEAR] == 1 &&
                current[Calendar.DAY_OF_YEAR] in 365..366
            )
    ) {
        context.getString(R.string.daily_tomorrow)
    } else if (date < current.time) { // In the past? Show full date
        date.getFormattedDate(getLongWeekdayDayMonth(context), location, context)
    } else {
        date.getWeek(location, context, full = true)
    }.capitalize(context.currentLocale)
}

fun Daily.getWeek(location: Location, context: Context?, full: Boolean = false): String {
    return date.getWeek(location, context, full)
}

fun Daily.isToday(location: Location): Boolean {
    val current = Calendar.getInstance(location.timeZone)
    val thisDay = Calendar.getInstance(location.timeZone)
    thisDay.time = date
    return current[Calendar.YEAR] == thisDay[Calendar.YEAR] &&
        current[Calendar.DAY_OF_YEAR] == thisDay[Calendar.DAY_OF_YEAR]
}

fun Daily.getTrendTemperature(context: Context, temperatureUnit: TemperatureUnit): String? {
    if (day?.temperature?.temperature == null || night?.temperature?.temperature == null) {
        return null
    }
    return day!!.temperature!!.temperature!!.formatMeasure(
        context,
        temperatureUnit,
        valueWidth = org.breezyweather.unit.formatting.UnitWidth.NARROW,
        unitWidth = org.breezyweather.unit.formatting.UnitWidth.NARROW
    ) +
        "/" +
        night!!.temperature!!.temperature!!.formatMeasure(
            context,
            temperatureUnit,
            valueWidth = org.breezyweather.unit.formatting.UnitWidth.NARROW,
            unitWidth = org.breezyweather.unit.formatting.UnitWidth.NARROW
        )
}
