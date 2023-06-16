package org.breezyweather.common.basic.models.weather

import android.content.Context
import org.breezyweather.R
import org.breezyweather.common.utils.helpers.LunarHelper
import java.io.Serializable
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

/**
 * Daily.
 */
class Daily(
    val date: Date,
    val day: HalfDay? = null,
    val night: HalfDay? = null,
    val sun: Astro? = null,
    val moon: Astro? = null,
    val moonPhase: MoonPhase? = null,
    val airQuality: AirQuality? = null,
    val pollen: Pollen? = null,
    val uV: UV? = null,
    val hoursOfSun: Float? = null
) : Serializable {
    fun getLongDate(context: Context, timeZone: TimeZone): String {
        return getDate(context.getString(R.string.date_format_long), timeZone)
    }

    fun getShortDate(context: Context, timeZone: TimeZone): String {
        return getDate(context.getString(R.string.date_format_short), timeZone)
    }

    fun getDate(format: String?, timeZone: TimeZone): String {
        return org.breezyweather.common.utils.DisplayUtils.getFormattedDate(date, timeZone, format)
    }

    fun getWeek(context: Context, timeZone: TimeZone): String {
        val calendar = Calendar.getInstance(timeZone)
        calendar.time = date
        return when (calendar[Calendar.DAY_OF_WEEK]) {
            1 -> context.getString(R.string.week_7)
            2 -> context.getString(R.string.week_1)
            3 -> context.getString(R.string.week_2)
            4 -> context.getString(R.string.week_3)
            5 -> context.getString(R.string.week_4)
            6 -> context.getString(R.string.week_5)
            else -> context.getString(R.string.week_6)
        }
    }

    val lunar: String
        get() = LunarHelper.getLunarDate(date)

    fun isToday(timeZone: TimeZone): Boolean {
        val current = Calendar.getInstance(timeZone)
        val thisDay = Calendar.getInstance(timeZone)
        thisDay.time = date
        return (current[Calendar.YEAR] == thisDay[Calendar.YEAR]
                && current[Calendar.DAY_OF_YEAR] == thisDay[Calendar.DAY_OF_YEAR])
    }
}
