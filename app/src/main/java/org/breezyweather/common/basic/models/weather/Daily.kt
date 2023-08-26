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

package org.breezyweather.common.basic.models.weather

import android.content.Context
import org.breezyweather.R
import org.breezyweather.common.utils.helpers.LunarHelper
import java.io.Serializable
import java.util.*

/**
 * Daily.
 */
data class Daily(
    /**
     * Daily date initialized at 00:00 in the TimeZone of the location
     */
    val date: Date,
    val day: HalfDay? = null,
    val night: HalfDay? = null,
    val degreeDay: DegreeDay? = null,
    val sun: Astro? = null,
    val moon: Astro? = null,
    val moonPhase: MoonPhase? = null,
    val airQuality: AirQuality? = null,
    val pollen: Pollen? = null,
    val uV: UV? = null,
    val hoursOfSun: Float? = null
) : Serializable {

    fun getWeek(context: Context, timeZone: TimeZone): String {
        val calendar = Calendar.getInstance(timeZone)
        calendar.time = date
        return when (calendar[Calendar.DAY_OF_WEEK]) {
            1 -> context.getString(R.string.short_sunday)
            2 -> context.getString(R.string.short_monday)
            3 -> context.getString(R.string.short_tuesday)
            4 -> context.getString(R.string.short_wednesday)
            5 -> context.getString(R.string.short_thursday)
            6 -> context.getString(R.string.short_friday)
            else -> context.getString(R.string.short_saturday)
        }
    }

    val lunar: String?
        get() = LunarHelper.getLunarDate(date)

    fun isToday(timeZone: TimeZone): Boolean {
        val current = Calendar.getInstance(timeZone)
        val thisDay = Calendar.getInstance(timeZone)
        thisDay.time = date
        return (current[Calendar.YEAR] == thisDay[Calendar.YEAR]
                && current[Calendar.DAY_OF_YEAR] == thisDay[Calendar.DAY_OF_YEAR])
    }
}
