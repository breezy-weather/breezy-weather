/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.common.utils.helpers

import com.xhinliang.lunarcalendar.LunarCalendar
import java.util.Calendar
import java.util.Date

/**
 * Lunar helper.
 */
object LunarHelper {
    fun getLunarDate(date: Date): String? {
        return getLunarDate(
            Calendar.getInstance().apply {
                time = date
            }
        )
    }

    private fun getLunarDate(calendar: Calendar): String? {
        return getLunarDate(
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH] + 1,
            calendar[Calendar.DAY_OF_MONTH]
        )
    }

    private fun getLunarDate(year: Int, month: Int, day: Int): String? {
        return try {
            val lunarCalendar = LunarCalendar.obtainCalendar(year, month, day)
            lunarCalendar.fullLunarStr.split("年".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[1]
                .replace("廿十", "二十")
                .replace("卅十", "三十")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
