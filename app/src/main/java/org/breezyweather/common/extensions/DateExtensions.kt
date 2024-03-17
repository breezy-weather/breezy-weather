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
import android.icu.util.TimeZone
import android.os.Build
import android.text.format.DateFormat
import android.text.format.DateUtils
import androidx.annotation.RequiresApi
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

@RequiresApi(Build.VERSION_CODES.N)
fun Date.getWeek(context: Context, timeZone: TimeZone): String {
    val locale = SettingsManager.getInstance(context).language.locale
    return android.icu.text.SimpleDateFormat("E", locale)
        .apply {
            setTimeZone(timeZone)
        }
        .format(this)
        .replaceFirstChar { firstChar ->
            if (firstChar.isLowerCase()) {
                firstChar.titlecase(locale)
            } else firstChar.toString()
        }
}
