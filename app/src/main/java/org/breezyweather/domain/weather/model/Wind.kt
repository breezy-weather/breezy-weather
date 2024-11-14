package org.breezyweather.domain.weather.model

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import breezyweather.domain.weather.model.Wind
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.unit.SpeedUnit

@ColorInt
fun Wind.getColor(context: Context): Int {
    if (speed == null) return Color.TRANSPARENT
    return when (speed!!) {
        in 0.0..Wind.WIND_SPEED_3 -> ContextCompat.getColor(context, R.color.colorLevel_1)
        in Wind.WIND_SPEED_3..Wind.WIND_SPEED_5 -> ContextCompat.getColor(context, R.color.colorLevel_2)
        in Wind.WIND_SPEED_5..Wind.WIND_SPEED_7 -> ContextCompat.getColor(context, R.color.colorLevel_3)
        in Wind.WIND_SPEED_7..Wind.WIND_SPEED_9 -> ContextCompat.getColor(context, R.color.colorLevel_4)
        in Wind.WIND_SPEED_9..Wind.WIND_SPEED_11 -> ContextCompat.getColor(context, R.color.colorLevel_5)
        in Wind.WIND_SPEED_11..Double.MAX_VALUE -> ContextCompat.getColor(context, R.color.colorLevel_6)
        else -> Color.TRANSPARENT
    }
}

fun Wind.getDirection(context: Context): String? {
    if (degree == null) return null
    return when (degree!!) {
        in 0.0..22.5 -> context.getString(R.string.wind_direction_short_N)
        in 22.5..67.5 -> context.getString(R.string.wind_direction_short_NE)
        in 67.5..112.5 -> context.getString(R.string.wind_direction_short_E)
        in 112.5..157.5 -> context.getString(R.string.wind_direction_short_SE)
        in 157.5..202.5 -> context.getString(R.string.wind_direction_short_S)
        in 202.5..247.5 -> context.getString(R.string.wind_direction_short_SW)
        in 247.5..292.5 -> context.getString(R.string.wind_direction_short_W)
        in 292.5..337.5 -> context.getString(R.string.wind_direction_short_NW)
        in 337.5..360.0 -> context.getString(R.string.wind_direction_short_N)
        else -> context.getString(R.string.wind_direction_short_variable)
    }
}

fun Wind.getStrength(context: Context): String? {
    if (speed == null) return null
    return when (speed!!) {
        in 0.0..Wind.WIND_SPEED_0 -> context.getString(R.string.wind_strength_0)
        in Wind.WIND_SPEED_0..Wind.WIND_SPEED_1 -> context.getString(R.string.wind_strength_1)
        in Wind.WIND_SPEED_1..Wind.WIND_SPEED_2 -> context.getString(R.string.wind_strength_2)
        in Wind.WIND_SPEED_2..Wind.WIND_SPEED_3 -> context.getString(R.string.wind_strength_3)
        in Wind.WIND_SPEED_3..Wind.WIND_SPEED_4 -> context.getString(R.string.wind_strength_4)
        in Wind.WIND_SPEED_4..Wind.WIND_SPEED_5 -> context.getString(R.string.wind_strength_5)
        in Wind.WIND_SPEED_5..Wind.WIND_SPEED_6 -> context.getString(R.string.wind_strength_6)
        in Wind.WIND_SPEED_6..Wind.WIND_SPEED_7 -> context.getString(R.string.wind_strength_7)
        in Wind.WIND_SPEED_7..Wind.WIND_SPEED_8 -> context.getString(R.string.wind_strength_8)
        in Wind.WIND_SPEED_8..Wind.WIND_SPEED_9 -> context.getString(R.string.wind_strength_9)
        in Wind.WIND_SPEED_9..Wind.WIND_SPEED_10 -> context.getString(R.string.wind_strength_10)
        in Wind.WIND_SPEED_10..Wind.WIND_SPEED_11 -> context.getString(R.string.wind_strength_11)
        in Wind.WIND_SPEED_11..Double.MAX_VALUE -> context.getString(R.string.wind_strength_12)
        else -> null
    }
}

fun Wind.getShortDescription(context: Context, unit: SpeedUnit): String? {
    val builder = StringBuilder()
    arrow?.let {
        builder.append(it)
    }
    speed?.let {
        if (builder.toString().isNotEmpty()) builder.append(" ")
        builder.append(unit.getValueText(context, it))
    }
    return builder.toString().ifEmpty { null }
}

fun Wind.getDescription(context: Context, unit: SpeedUnit): String {
    val builder = StringBuilder()
    if (!getDirection(context).isNullOrEmpty()) {
        builder.append(getDirection(context))
    }
    speed?.let {
        if (builder.toString().isNotEmpty()) builder.append(" ")
        builder.append(unit.getValueText(context, it))
    }
    if (!getStrength(context).isNullOrEmpty()) {
        if (builder.toString().isNotEmpty()) builder.append(" ")
        builder.append("(").append(getStrength(context)).append(")")
    }
    arrow?.let {
        if (builder.toString().isNotEmpty()) builder.append(" ")
        builder.append(it)
    }
    return builder.toString()
}
