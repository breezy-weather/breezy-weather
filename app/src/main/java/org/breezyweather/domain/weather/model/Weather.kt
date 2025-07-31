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
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Normals
import breezyweather.domain.weather.model.Weather
import breezyweather.domain.weather.reference.Month
import org.breezyweather.R
import org.breezyweather.common.extensions.getCalendarMonth
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.domain.settings.SettingsManager
import java.util.Calendar
import java.util.Date

val Weather.validAirQuality: AirQuality?
    get() = if (current?.airQuality != null && current!!.airQuality!!.isIndexValid) {
        current!!.airQuality
    } else if (today?.airQuality != null && today!!.airQuality!!.isIndexValid) {
        today!!.airQuality
    } else {
        null
    }

val Weather.hasMinutelyPrecipitation: Boolean
    get() = minutelyForecast.any { (it.dbz ?: 0) > 0 }

fun Weather.getMinutelyTitle(context: Context): String {
    return if (hasMinutelyPrecipitation) {
        // 1 = soon, 2 = continue, 3 = end
        val case = if (minutelyForecast.first().dbz != null && minutelyForecast.first().dbz!! > 0) {
            if (minutelyForecast.last().dbz != null && minutelyForecast.last().dbz!! > 0) 2 else 3
        } else {
            1
        }

        when (case) {
            1 -> context.getString(R.string.notification_precipitation_starting)
            2 -> context.getString(R.string.notification_precipitation_continuing)
            3 -> context.getString(R.string.notification_precipitation_stopping)
            else -> context.getString(R.string.precipitation)
        }
    } else {
        context.getString(R.string.precipitation)
    }
}

fun Weather.getMinutelyDescription(context: Context, location: Location): String {
    return if (hasMinutelyPrecipitation) {
        // 1 = soon, 2 = continue, 3 = end
        val case = if (minutelyForecast.first().dbz != null && minutelyForecast.first().dbz!! > 0) {
            if (minutelyForecast.last().dbz != null && minutelyForecast.last().dbz!! > 0) 2 else 3
        } else {
            1
        }

        context.getString(
            when (case) {
                1 -> R.string.notification_precipitation_starting_desc
                2 -> R.string.notification_precipitation_continuing_desc
                3 -> R.string.notification_precipitation_stopping_desc
                else -> R.string.notification_precipitation_continuing_desc
            },
            when (case) {
                1 -> minutelyForecast.first { (it.dbz ?: 0) > 0 }.date
                    .getFormattedTime(location, context, context.is12Hour)
                else -> minutelyForecast.last { (it.dbz ?: 0) > 0 }.endingDate
                    .getFormattedTime(location, context, context.is12Hour)
            }
        )
    } else {
        context.getString(R.string.precipitation_none)
    }
}

fun Weather.getTemperatureRangeSummary(context: Context, location: Location): Pair<String, String>? {
    if (today == null) return null

    val cal = Date().toCalendarWithTimeZone(location.javaTimeZone)
    val currentHour = cal[Calendar.HOUR_OF_DAY]

    val isDayFirst: Boolean
    val temperatures = mutableListOf<Double?>()

    val halfDayTemperatureRange = mutableListOf<String>()
    val halfDayTemperatureRangeVoice = mutableListOf<String>()
    val temperatureUnit = SettingsManager.getInstance(context).getTemperatureUnit(context)

    // Early morning
    if (currentHour < 6) {
        val yesterday = dailyForecast.getOrElse(todayIndex!!.minus(1)) { null }
        isDayFirst = false
        temperatures.add(yesterday?.night?.temperature?.temperature)
        temperatures.add(today!!.day?.temperature?.temperature)
    } else if (currentHour < 18) {
        isDayFirst = true
        temperatures.add(today!!.day?.temperature?.temperature)
        temperatures.add(today!!.night?.temperature?.temperature)
    } else {
        isDayFirst = false
        temperatures.add(today!!.night?.temperature?.temperature)
        temperatures.add(tomorrow?.day?.temperature?.temperature)
    }

    temperatures.getOrElse(0) { null }?.let {
        halfDayTemperatureRange.add(
            context.getString(if (isDayFirst) R.string.daytime_short else R.string.nighttime_short) +
                context.getString(R.string.colon_separator) +
                temperatureUnit.formatMeasureShort(context, it)
        )
        halfDayTemperatureRangeVoice.add(
            context.getString(if (isDayFirst) R.string.daytime_short else R.string.nighttime_short) +
                context.getString(R.string.colon_separator) +
                temperatureUnit.formatContentDescription(context, it)
        )
    }

    temperatures.getOrElse(1) { null }?.let {
        halfDayTemperatureRange.add(
            context.getString(if (isDayFirst) R.string.nighttime_short else R.string.daytime_short) +
                context.getString(R.string.colon_separator) +
                temperatureUnit.formatMeasureShort(context, it)
        )
        halfDayTemperatureRangeVoice.add(
            context.getString(if (isDayFirst) R.string.nighttime_short else R.string.daytime_short) +
                context.getString(R.string.colon_separator) +
                temperatureUnit.formatContentDescription(context, it)
        )
    }

    return if (halfDayTemperatureRange.isNotEmpty()) {
        Pair(
            halfDayTemperatureRange.joinToString(context.getString(R.string.dot_separator)),
            halfDayTemperatureRangeVoice.joinToString(context.getString(R.string.dot_separator))
        )
    } else {
        null
    }
}
