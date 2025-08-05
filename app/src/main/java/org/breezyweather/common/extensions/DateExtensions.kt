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

import android.annotation.SuppressLint
import android.content.Context
import android.icu.text.DateTimePatternGenerator
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.icu.util.ULocale
import android.os.Build
import android.text.format.DateFormat
import android.text.format.DateUtils
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.reference.Month
import org.breezyweather.BreezyWeather
import org.breezyweather.common.basic.models.options.appearance.CalendarHelper
import org.breezyweather.common.utils.helpers.LogHelper
import org.chickenhook.restrictionbypass.RestrictionBypass
import java.lang.reflect.Method
import java.util.Calendar
import java.util.Date
import java.util.Locale

val Context.is12Hour: Boolean
    get() = !DateFormat.is24HourFormat(this)

@SuppressLint("PrivateApi")
fun Date.getRelativeTime(context: Context): String {
    try {
        // Reflection allows us to specify the locale
        // If we don't, we always have system locale instead of per-app language preference
        val getRelativeTimeSpanStringMethod: Method = RestrictionBypass.getMethod(
            Class.forName("android.text.format.RelativeDateTimeFormatter"),
            "getRelativeTimeSpanString",
            Locale::class.java,
            java.util.TimeZone::class.java,
            Long::class.javaPrimitiveType,
            Long::class.javaPrimitiveType,
            Long::class.javaPrimitiveType,
            Int::class.javaPrimitiveType
        )
        return getRelativeTimeSpanStringMethod.invoke(
            null,
            context.currentLocale,
            java.util.TimeZone.getDefault(),
            time,
            Date().time,
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        ) as String
    } catch (ignored: Exception) {
        if (BreezyWeather.instance.debugMode) {
            LogHelper.log(msg = "Reflection of relative time failed")
        }
        return DateUtils.getRelativeTimeSpanString(
            time,
            Date().time,
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        ) as String
    }
}

// Makes the code more readable by not having to do a null check condition
fun Long.toDate(): Date {
    return Date(this)
}

fun Date.getFormattedDate(
    pattern: String,
    location: Location? = null,
    context: Context? = null,
    withBestPattern: Boolean = false,
): String {
    val locale = context?.currentLocale ?: Locale.Builder().setLanguage("en").setRegion("001").build()
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        SimpleDateFormat(
            if (withBestPattern) {
                DateTimePatternGenerator.getInstance(locale).getBestPattern(pattern)
            } else {
                pattern
            },
            locale
        ).apply {
            timeZone = location?.timeZone?.let { TimeZone.getTimeZone(it) } ?: TimeZone.getDefault()
        }.format(this)
    } else {
        @Suppress("DEPRECATION")
        getFormattedDate(pattern, location?.javaTimeZone, locale)
    }
}

fun Date.getFormattedTime(
    location: Location? = null,
    context: Context?,
    twelveHour: Boolean,
): String {
    return if (twelveHour) {
        getFormattedDate("h:mm aa", location, context, withBestPattern = true)
    } else {
        getFormattedDate("HH:mm", location, context)
    }
}

fun Date.getFormattedShortDayAndMonth(
    location: Location,
    context: Context?,
): String {
    return getFormattedDate("MM-dd", location, context, withBestPattern = true)
}

fun Date.getFormattedDayOfTheMonth(
    location: Location,
    context: Context?,
): String {
    return getFormattedDate("dd", location, context, withBestPattern = true)
}

fun Date.getFormattedMediumDayAndMonth(
    location: Location,
    context: Context?,
): String {
    val locale = context?.currentLocale ?: Locale.Builder().setLanguage("en").setRegion("001").build()
    return getFormattedDate("d MMM", location, context, withBestPattern = true).capitalize(locale)
}

fun Date.getFormattedFullDayAndMonth(
    location: Location,
    context: Context?,
): String {
    val locale = context?.currentLocale ?: Locale.Builder().setLanguage("en").setRegion("001").build()
    return getFormattedDate("d MMMM", location, context, withBestPattern = true).capitalize(locale)
}

fun getShortWeekdayDayMonth(
    context: Context?,
): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        DateTimePatternGenerator.getInstance(
            context?.currentLocale ?: Locale.Builder().setLanguage("en").setRegion("001").build()
        ).getBestPattern("EEE d MMM")
    } else {
        "EEE d MMM"
    }
}

fun getLongWeekdayDayMonth(
    context: Context?,
): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        DateTimePatternGenerator.getInstance(
            context?.currentLocale ?: Locale.Builder().setLanguage("en").setRegion("001").build()
        ).getBestPattern("EEEE d MMMM")
    } else {
        "EEEE d MMMM"
    }
}

fun Date.getWeek(location: Location, context: Context?, full: Boolean = false): String {
    val locale = context?.currentLocale ?: Locale.Builder().setLanguage("en").setRegion("001").build()
    return getFormattedDate(if (full) "EEEE" else "E", location, context).capitalize(locale)
}

fun Date.getHour(location: Location, context: Context): String {
    return getFormattedDate(
        if (context.is12Hour) "h aa" else "H:mm",
        location,
        context,
        withBestPattern = context.is12Hour
    )
}

fun Date.getHourIn24Format(location: Location): String {
    return getFormattedDate("H", location)
}

/**
 * See CalendarHelper.supportedCalendars for full list of supported calendars
 */
fun Date.getFormattedMediumDayAndMonthInAdditionalCalendar(
    location: Location? = null,
    context: Context,
): String? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val calendarId = CalendarHelper.getAlternateCalendarSetting(context)
        if (calendarId != null) {
            val alternateCalendar = CalendarHelper.getCalendars(context).firstOrNull { it.id == calendarId }
            if (alternateCalendar != null) {
                val locale = context.currentLocale
                val uLocale = ULocale.Builder().apply {
                    setLanguageTag(locale.toLanguageTag())
                    setUnicodeLocaleKeyword(CalendarHelper.CALENDAR_EXTENSION_TYPE, calendarId)
                    alternateCalendar.additionalParams?.forEach {
                        setUnicodeLocaleKeyword(it.key, it.value)
                    }
                }.build()
                SimpleDateFormat(
                    if (!alternateCalendar.specificPattern.isNullOrEmpty()) {
                        alternateCalendar.specificPattern
                    } else {
                        DateTimePatternGenerator.getInstance(uLocale).getBestPattern("d MMM")
                    },
                    uLocale
                ).apply {
                    timeZone = location?.timeZone?.let { TimeZone.getTimeZone(it) } ?: TimeZone.getDefault()
                }.format(this)
            } else {
                null
            }
        } else {
            null
        }
    } else {
        null
    }
}

fun Date.toCalendar(location: Location): Calendar {
    return Calendar.getInstance().also {
        it.time = this
        it.timeZone = location.javaTimeZone
    }
}

/**
 * Optimized function to get yyyy-MM-dd formatted date
 * Takes 0 ms on my device compared to 2-3 ms for getFormattedDate() (which uses SimpleDateFormat)
 * Saves about 1 second when looping through 24 hourly over a 16 day period
 */
fun Calendar.getIsoFormattedDate(): String {
    return "${this[Calendar.YEAR]}-${getMonth(twoDigits = true)}-${getDayOfMonth(twoDigits = true)}"
}

fun Calendar.getMonth(twoDigits: Boolean = false): String {
    return "${(this[Calendar.MONTH] + 1).let { month ->
        if (twoDigits && month.toString().length < 2) "0$month" else month
    }}"
}

fun Calendar.getDayOfMonth(twoDigits: Boolean = false): String {
    return "${this[Calendar.DAY_OF_MONTH].let { day ->
        if (twoDigits && day.toString().length < 2) "0$day" else day
    }}"
}

fun Date.getIsoFormattedDate(location: Location): String {
    return toCalendar(location).getIsoFormattedDate()
}

fun Date.getCalendarMonth(location: Location): Month {
    return Month.fromCalendarMonth(toCalendarWithTimeZone(location.javaTimeZone)[Calendar.MONTH])
}
