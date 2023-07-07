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
