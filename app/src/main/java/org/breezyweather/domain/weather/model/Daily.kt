package org.breezyweather.domain.weather.model

import android.content.Context
import breezyweather.domain.weather.model.Daily
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.utils.helpers.LunarHelper
import java.util.Calendar
import java.util.TimeZone

fun Daily.getWeek(context: Context, timeZone: TimeZone): String {
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

val Daily.lunar: String?
    get() = LunarHelper.getLunarDate(date)

fun Daily.isToday(timeZone: TimeZone): Boolean {
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