package org.breezyweather.common.basic.models.weather

import android.annotation.SuppressLint
import android.content.Context
import android.text.BidiFormatter
import org.breezyweather.R
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.isRtl
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import java.io.Serializable
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

/**
 * Hourly.
 */
data class Hourly(
    val date: Date,
    val isDaylight: Boolean = true,
    val weatherText: String? = null,
    val weatherCode: WeatherCode? = null,
    val temperature: Temperature? = null,
    val precipitation: Precipitation? = null,
    val precipitationProbability: PrecipitationProbability? = null,
    val wind: Wind? = null,
    val airQuality: AirQuality? = null,
    val pollen: Pollen? = null,
    val uV: UV? = null
) : Serializable {

    fun getHourIn24Format(timeZone: TimeZone): Int {
        val calendar = date.toCalendarWithTimeZone(timeZone)
        return calendar[Calendar.HOUR_OF_DAY]
    }

    fun getHour(context: Context, timeZone: TimeZone): String {
        return getHour(context, timeZone, context.is12Hour, context.isRtl)
    }

    @SuppressLint("DefaultLocale")
    private fun getHour(context: Context, timeZone: TimeZone, twelveHour: Boolean, rtl: Boolean): String {
        val calendar = date.toCalendarWithTimeZone(timeZone)
        var hour: Int
        if (twelveHour) {
            hour = calendar[Calendar.HOUR]
            if (hour == 0) {
                hour = 12
            }
        } else {
            hour = calendar[Calendar.HOUR_OF_DAY]
        }
        return if (rtl) {
            (BidiFormatter.getInstance().unicodeWrap(String.format("%d", hour))
                    + context.getString(R.string.of_clock))
        } else {
            hour.toString() + context.getString(R.string.of_clock)
        }
    }
}