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
import org.breezyweather.settings.SettingsManager
import java.util.Date
import java.util.Locale

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
    location: Location,
    pattern: String,
    locale: Locale = Locale.getDefault(),
    withBestPattern: Boolean = false
): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        SimpleDateFormat(
            if (withBestPattern) {
                DateTimePatternGenerator.getInstance(locale).getBestPattern(pattern)
            } else pattern,
            locale
        )
            .apply {
                timeZone = location.icuTimeZone
            }
            .format(this)
    } else this.getFormattedDate(location.javaTimeZone, pattern, locale)
}

fun Date.getFormattedShortDayAndMonth(
    location: Location,
    locale: Locale = Locale.getDefault()
): String {
    return this.getFormattedDate(location, "MM-dd", locale, withBestPattern = true)
}

fun Date.getFormattedMediumDayAndMonth(
    location: Location,
    locale: Locale = Locale.getDefault()
): String {
    return this.getFormattedDate(location, "d MMM", locale, withBestPattern = true)
}

fun Date.getWeek(context: Context, location: Location): String {
    val locale = SettingsManager.getInstance(context).language.locale
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        SimpleDateFormat("E", locale)
            .apply {
                timeZone = location.icuTimeZone
            }
            .format(this)
            .replaceFirstChar { firstChar ->
                if (firstChar.isLowerCase()) {
                    firstChar.titlecase(locale)
                } else firstChar.toString()
            }
    } else this.getWeek(locale, location.javaTimeZone)
}
