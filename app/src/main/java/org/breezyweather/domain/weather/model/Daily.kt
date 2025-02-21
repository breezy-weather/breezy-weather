package org.breezyweather.domain.weather.model

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Daily
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.extensions.getWeek
import java.util.Calendar

fun Daily.getWeek(location: Location, context: Context?, full: Boolean = false): String {
    return date.getWeek(location, context, full)
}

fun Daily.isToday(location: Location): Boolean {
    val current = Calendar.getInstance(location.javaTimeZone) // TODO: Use ICU
    val thisDay = Calendar.getInstance(location.javaTimeZone) // TODO: Use ICU
    thisDay.time = date
    return current[Calendar.YEAR] == thisDay[Calendar.YEAR] &&
        current[Calendar.DAY_OF_YEAR] == thisDay[Calendar.DAY_OF_YEAR]
}

fun Daily.getTrendTemperature(context: Context, unit: TemperatureUnit): String? {
    if (day?.temperature?.temperature == null || night?.temperature?.temperature == null) {
        return null
    }
    return unit.getShortValueText(context, day!!.temperature!!.temperature!!) + "/" +
        unit.getShortValueText(context, night!!.temperature!!.temperature!!)
}
