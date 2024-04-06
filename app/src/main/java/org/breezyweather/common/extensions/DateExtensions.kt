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
import android.icu.text.DateTimePatternGenerator
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.icu.util.ULocale
import android.os.Build
import android.text.format.DateFormat
import android.text.format.DateUtils
import breezyweather.domain.location.model.Location
import com.xhinliang.lunarcalendar.LunarCalendar
import org.breezyweather.BreezyWeather
import org.breezyweather.R
import org.breezyweather.common.utils.helpers.LogHelper
import org.breezyweather.settings.SettingsManager
import org.chickenhook.restrictionbypass.RestrictionBypass
import java.lang.reflect.Method
import java.util.Calendar
import java.util.Date
import java.util.Locale

val Context.is12Hour: Boolean
    get() = !DateFormat.is24HourFormat(this)

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
            Int::class.javaPrimitiveType,
        )
        return getRelativeTimeSpanStringMethod.invoke(
            null,
            context.currentLocale,
            java.util.TimeZone.getDefault(),
            this.time,
            Date().time,
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        ) as String
    } catch (ignored: Exception) {
        if (BreezyWeather.instance.debugMode) {
            LogHelper.log(msg = "Reflection of relative time failed")
        }
        return DateUtils.getRelativeTimeSpanString(
            this.time,
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
    withBestPattern: Boolean = false
): String {
    val locale = context?.currentLocale ?: Locale("en", "001")
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        SimpleDateFormat(
            if (withBestPattern) {
                DateTimePatternGenerator.getInstance(locale).getBestPattern(pattern)
            } else pattern,
            locale
        ).apply {
            timeZone = location?.timeZone?.let { TimeZone.getTimeZone(it) } ?: TimeZone.getDefault()
        }.format(this)
    } else {
        this.getFormattedDate(
            pattern, location?.javaTimeZone, locale
        )
    }
}

fun Date.getFormattedTime(
    location: Location? = null,
    context: Context?,
    twelveHour: Boolean
): String {
    return if (twelveHour) {
        this.getFormattedDate("h:mm aa", location, context)
    } else this.getFormattedDate("HH:mm", location, context)
}

fun Date.getFormattedShortDayAndMonth(
    location: Location,
    context: Context?
): String {
    return this.getFormattedDate("MM-dd", location, context, withBestPattern = true)
}

fun Date.getFormattedMediumDayAndMonth(
    location: Location,
    context: Context?
): String {
    return this.getFormattedDate("d MMM", location, context, withBestPattern = true)
}

fun getShortWeekdayDayMonth(
    context: Context?
): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        DateTimePatternGenerator.getInstance(
            context?.currentLocale ?: Locale("en", "001")
        ).getBestPattern("EEE d MMM")
    } else "EEE d MMM"
}

fun getLongWeekdayDayMonth(
    context: Context?
): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        DateTimePatternGenerator.getInstance(
            context?.currentLocale ?: Locale("en", "001")
        ).getBestPattern("EEEE d MMMM")
    } else "EEEE d MMMM"
}

fun Date.getWeek(location: Location, context: Context?): String {
    return getFormattedDate("E", location, context)
}

fun Date.getHour(location: Location, context: Context): String {
    return getFormattedDate(
        if (context.is12Hour) "h aa" else "H", location, context
    ) + if (!context.is12Hour) context.getString(R.string.of_clock) else ""
}

fun Date.getHourIn24Format(location: Location): String {
    return getFormattedDate("H", location)
}

/**
 * Currently only handles Chinese calendar
 */
fun Date.getFormattedMediumDayAndMonthInAdditionalCalendar(
    location: Location? = null, context: Context
): String? {
    /*return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        //val calendar = SettingsManager.getInstance(context).additionalCalendar
        //if (calendar.isNullOrEmpty()) return null
        //if (calendar == "auto") {  } // TODO
        val locale = context.currentLocale
        val uLocale = ULocale("${locale.toLanguageTag()}@calendar=chinese")
        SimpleDateFormat(
            DateTimePatternGenerator.getInstance(uLocale).getBestPattern("d MMM"),
            uLocale
        ).apply {
            timeZone = location?.timeZone?.let { TimeZone.getTimeZone(it) } ?: TimeZone.getDefault()
        }.format(this)
    } else {*/
        val cal = Calendar.getInstance().apply {
            time = this@getFormattedMediumDayAndMonthInAdditionalCalendar
            timeZone = location?.timeZone?.let { java.util.TimeZone.getTimeZone(it) } ?: java.util.TimeZone.getDefault()
        }
        return try {
            val lunarCalendar = LunarCalendar.obtainCalendar(
                cal[Calendar.YEAR],
                cal[Calendar.MONTH] + 1,
                cal[Calendar.DAY_OF_MONTH]
            )
            lunarCalendar.fullLunarStr.split("年".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[1]
                .replace("廿十", "二十")
                .replace("卅十", "三十")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    //}
}
