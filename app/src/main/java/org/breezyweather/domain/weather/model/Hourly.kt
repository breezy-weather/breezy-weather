package org.breezyweather.domain.weather.model

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
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

@ColorInt
fun Hourly.getCloudCoverColor(context: Context): Int {
    if (cloudCover == null) return Color.TRANSPARENT
    return when (cloudCover!!.toDouble()) {
        in 0.0..CLOUD_COVER_CLEAR -> ContextCompat.getColor(context, R.color.colorLevel_1)
        in CLOUD_COVER_CLEAR..CLOUD_COVER_PARTLY -> ContextCompat.getColor(context, R.color.colorLevel_2)
        in CLOUD_COVER_PARTLY..100.0 -> ContextCompat.getColor(context, R.color.colorLevel_3)
        else -> Color.TRANSPARENT
    }
}

const val CLOUD_COVER_CLEAR = 37.5
const val CLOUD_COVER_PARTLY = 75.0