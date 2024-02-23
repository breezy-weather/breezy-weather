package org.breezyweather.domain.location.model

import android.content.Context
import breezyweather.domain.location.model.Location
import org.breezyweather.domain.weather.model.getRiseProgress
import org.breezyweather.R

fun Location.getPlace(context: Context, showCurrentPositionInPriority: Boolean = false): String {
    if (showCurrentPositionInPriority && isCurrentPosition) {
        return context.getString(R.string.location_current)
    }
    val builder = StringBuilder()
    builder.append(cityAndDistrict)
    if (builder.toString().isEmpty() && isCurrentPosition) {
        return context.getString(R.string.location_current)
    }
    return builder.toString()
}

val Location.isDaylight: Boolean
    get() {
        val sunRiseProgress = getRiseProgress(
            astro = this.weather?.today?.sun,
            timeZone = this.timeZone
        )
        return 0 < sunRiseProgress && sunRiseProgress < 1
    }
