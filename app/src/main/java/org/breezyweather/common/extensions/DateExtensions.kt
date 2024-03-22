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
import android.os.Build
import android.text.format.DateFormat
import android.text.format.DateUtils
import breezyweather.domain.location.model.Location
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.appearance.Language
import org.breezyweather.settings.SettingsManager
import java.util.Date

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

// Makes the code more readable by not having to do a null check condition
fun Long.toDate(): Date {
    return Date(this)
}

fun Date.getFormattedDate(
    pattern: String,
    location: Location = Location(),
    language: Language = Language.ENGLISH_US,
    withBestPattern: Boolean = false
): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        SimpleDateFormat(
            if (withBestPattern) {
                DateTimePatternGenerator.getInstance(language.locale).getBestPattern(pattern)
            } else pattern,
            language.locale
        ).apply {
            timeZone = location.icuTimeZone
        }.format(this)
    } else this.getFormattedDate(pattern, location.javaTimeZone, language.locale)
}

fun Date.getFormattedTime(
    location: Location,
    language: Language,
    twelveHour: Boolean
): String {
    return if (twelveHour) {
        this.getFormattedDate("h:mm aa", location, language)
    } else this.getFormattedDate("HH:mm", location, language)
}

fun Date.getFormattedShortDayAndMonth(
    location: Location,
    language: Language
): String {
    return this.getFormattedDate("MM-dd", location, language, withBestPattern = true)
}

fun Date.getFormattedMediumDayAndMonth(
    location: Location,
    language: Language
): String {
    return this.getFormattedDate("d MMM", location, language, withBestPattern = true)
}

fun getShortWeekdayDayMonth(
    language: Language
): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        DateTimePatternGenerator.getInstance(language.locale).getBestPattern("EEE d MMM")
    } else "EEE d MMM"
}

fun getLongWeekdayDayMonth(
    language: Language
): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        DateTimePatternGenerator.getInstance(language.locale).getBestPattern("EEEE d MMMM")
    } else "EEEE d MMMM"
}

fun Date.getWeek(location: Location, language: Language): String {
    return getFormattedDate("E", location, language)
}

fun Date.getHour(location: Location, context: Context): String {
    return getFormattedDate(
        if (context.is12Hour) "h aa" else "H",
        location,
        SettingsManager.getInstance(context).language
    ) + if (!context.is12Hour) context.getString(R.string.of_clock) else ""
}

fun Date.getHourIn24Format(location: Location): String {
    return getFormattedDate("H", location)
}
