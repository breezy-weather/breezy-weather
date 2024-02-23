package org.breezyweather.domain.weather.model

import android.content.Context
import breezyweather.domain.weather.model.Hourly
import org.breezyweather.R
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import java.util.Calendar
import java.util.TimeZone

fun Hourly.getHourIn24Format(timeZone: TimeZone): Int {
    val calendar = date.toCalendarWithTimeZone(timeZone)
    return calendar[Calendar.HOUR_OF_DAY]
}

fun Hourly.getHour(context: Context, timeZone: TimeZone): String {
    return date.getFormattedDate(timeZone, if (context.is12Hour) "h aa" else "H") +
            if (!context.is12Hour) context.getString(R.string.of_clock) else ""
}
