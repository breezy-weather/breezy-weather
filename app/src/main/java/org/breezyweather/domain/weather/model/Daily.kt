package org.breezyweather.domain.weather.model

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import breezyweather.domain.weather.model.Daily
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.extensions.getWeek
import org.breezyweather.common.utils.helpers.LunarHelper
import java.util.Calendar

@Deprecated("Use ICU functions instead")
fun Daily.getWeek(context: Context, timeZone: java.util.TimeZone): String {
    return date.getWeek(context, timeZone)
}

@RequiresApi(Build.VERSION_CODES.N)
fun Daily.getWeek(context: Context, timeZone: android.icu.util.TimeZone): String {
    return date.getWeek(context, timeZone)
}

val Daily.lunar: String?
    get() = LunarHelper.getLunarDate(date)

fun Daily.isToday(timeZone: java.util.TimeZone): Boolean {
    val current = Calendar.getInstance(timeZone)
    val thisDay = Calendar.getInstance(timeZone)
    thisDay.time = date
    return (current[Calendar.YEAR] == thisDay[Calendar.YEAR]
            && current[Calendar.DAY_OF_YEAR] == thisDay[Calendar.DAY_OF_YEAR])
}

fun Daily.getTrendTemperature(context: Context, unit: TemperatureUnit): String? {
    if (day?.temperature?.temperature == null || night?.temperature?.temperature == null) {
        return null
    }
    return unit.getShortValueText(context, day!!.temperature!!.temperature!!) + "/" + unit.getShortValueText(context, night!!.temperature!!.temperature!!)
}