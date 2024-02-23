/**
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
import android.text.format.DateFormat
import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

val Context.is12Hour: Boolean
    get() = !DateFormat.is24HourFormat(this)

val Date.relativeTime: String
    get() {
        return (DateUtils.getRelativeTimeSpanString(
            this.time,
            Date().time,
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        ) as String)
    }


fun Date.toCalendarWithTimeZone(zone: TimeZone): Calendar {
    return Calendar.getInstance().also {
        it.time = this
        it.timeZone = zone
    }
}

/**
 * Get a date at midnight on a specific timezone from a formatted date
 * @param timeZone
 * @param formattedDate in yyyy-MM-dd format
 * @return Date
 */
fun String.toDateNoHour(timeZoneP: TimeZone = TimeZone.getDefault()): Date? {
    if (this.isEmpty() || this.length != 10) return null
    return Calendar.getInstance().also {
        it.timeZone = timeZoneP
        it.set(Calendar.YEAR, this.substring(0, 4).toInt())
        it.set(Calendar.MONTH, this.substring(5, 7).toInt() - 1)
        it.set(Calendar.DAY_OF_MONTH, this.substring(8, 10).toInt())
        it.set(Calendar.HOUR_OF_DAY, 0)
        it.set(Calendar.MINUTE, 0)
        it.set(Calendar.SECOND, 0)
        it.set(Calendar.MILLISECOND, 0)
    }.time
}

fun Date.toTimezone(timeZone: TimeZone = TimeZone.getDefault()): Date {
    val calendarWithTimeZone = this.toCalendarWithTimeZone(timeZone)
    return Date(
        calendarWithTimeZone[Calendar.YEAR] - 1900,
        calendarWithTimeZone[Calendar.MONTH],
        calendarWithTimeZone[Calendar.DAY_OF_MONTH],
        calendarWithTimeZone[Calendar.HOUR_OF_DAY],
        calendarWithTimeZone[Calendar.MINUTE],
        calendarWithTimeZone[Calendar.SECOND]
    )
}

fun Date.toTimezoneNoHour(timeZone: TimeZone = TimeZone.getDefault()): Date? {
    return this.toCalendarWithTimeZone(timeZone).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time
}

fun Date.getFormattedDate(
    timeZone: TimeZone = TimeZone.getDefault(),
    pattern: String,
    locale: Locale = Locale.getDefault()
): String {
    return SimpleDateFormat(pattern, locale).format(this.toTimezone(timeZone))
}

fun Date.getFormattedTime(timeZone: TimeZone, twelveHour: Boolean): String {
    return if (twelveHour) {
        this.getFormattedDate(timeZone, "h:mm aa")
    } else {
        this.getFormattedDate(timeZone, "HH:mm")
    }
}

// Makes the code more readable by not having to do a null check condition
fun Long.toDate(): Date {
    return Date(this)
}