package org.breezyweather.domain.weather.model

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import breezyweather.domain.weather.model.Precipitation
import org.breezyweather.R

@ColorInt
fun Precipitation.getPrecipitationColor(context: Context): Int {
    return if (total == null) {
        Color.TRANSPARENT
    } else when (total!!) {
        in 0.0..Precipitation.PRECIPITATION_LIGHT -> ContextCompat.getColor(context, R.color.colorLevel_1)
        in Precipitation.PRECIPITATION_LIGHT..Precipitation.PRECIPITATION_MIDDLE -> ContextCompat.getColor(context, R.color.colorLevel_2)
        in Precipitation.PRECIPITATION_MIDDLE..Precipitation.PRECIPITATION_HEAVY -> ContextCompat.getColor(context, R.color.colorLevel_3)
        in Precipitation.PRECIPITATION_HEAVY..Precipitation.PRECIPITATION_RAINSTORM -> ContextCompat.getColor(context, R.color.colorLevel_4)
        in Precipitation.PRECIPITATION_RAINSTORM.. Double.MAX_VALUE -> ContextCompat.getColor(context, R.color.colorLevel_5)
        else -> Color.TRANSPARENT
    }
}