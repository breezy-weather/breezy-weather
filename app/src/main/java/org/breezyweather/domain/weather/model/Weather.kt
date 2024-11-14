package org.breezyweather.domain.weather.model

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Weather
import org.breezyweather.R
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour

val Weather.validAirQuality: AirQuality?
    get() = if (current?.airQuality != null && current!!.airQuality!!.isIndexValid) {
        current!!.airQuality
    } else if (today?.airQuality != null && today!!.airQuality!!.isIndexValid) {
        today!!.airQuality
    } else {
        null
    }

val Weather.hasMinutelyPrecipitation: Boolean
    get() = minutelyForecast.any { (it.dbz ?: 0) > 0 }

fun Weather.getMinutelyTitle(context: Context): String {
    return if (hasMinutelyPrecipitation) {
        // 1 = soon, 2 = continue, 3 = end
        val case = if (minutelyForecast.first().dbz != null && minutelyForecast.first().dbz!! > 0) {
            if (minutelyForecast.last().dbz != null && minutelyForecast.last().dbz!! > 0) 2 else 3
        } else {
            1
        }

        when (case) {
            1 -> context.getString(R.string.notification_precipitation_starting)
            2 -> context.getString(R.string.notification_precipitation_continuing)
            3 -> context.getString(R.string.notification_precipitation_stopping)
            else -> context.getString(R.string.precipitation)
        }
    } else {
        context.getString(R.string.precipitation)
    }
}

fun Weather.getMinutelyDescription(context: Context, location: Location): String {
    return if (hasMinutelyPrecipitation) {
        // 1 = soon, 2 = continue, 3 = end
        val case = if (minutelyForecast.first().dbz != null && minutelyForecast.first().dbz!! > 0) {
            if (minutelyForecast.last().dbz != null && minutelyForecast.last().dbz!! > 0) 2 else 3
        } else {
            1
        }

        context.getString(
            when (case) {
                1 -> R.string.notification_precipitation_starting_desc
                2 -> R.string.notification_precipitation_continuing_desc
                3 -> R.string.notification_precipitation_stopping_desc
                else -> R.string.notification_precipitation_continuing_desc
            },
            when (case) {
                1 -> minutelyForecast.first { (it.dbz ?: 0) > 0 }.date
                    .getFormattedTime(location, context, context.is12Hour)
                else -> minutelyForecast.last { (it.dbz ?: 0) > 0 }.date
                    .getFormattedTime(location, context, context.is12Hour)
            }
        )
    } else {
        context.getString(R.string.precipitation_none)
    }
}
