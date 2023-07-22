package org.breezyweather.common.basic.models.weather

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.unit.SpeedUnit
import java.io.Serializable

/**
 * DailyWind.
 *
 * default unit:
 * [.speed] : [SpeedUnit.KPH]
 */
class Wind(
    /**
     * Between 0 and 360, or -1 if variable
     */
    val degree: Float? = null,
    /**
     * In km/h
     */
    val speed: Float? = null
) : Serializable {

    val isValid: Boolean
        get() = speed != null && speed > 0

    @ColorInt
    fun getColor(context: Context): Int {
        return when (speed) {
            null -> Color.TRANSPARENT
            in 0f..WIND_SPEED_3 -> ContextCompat.getColor(context, R.color.colorLevel_1)
            in WIND_SPEED_3..WIND_SPEED_5 -> ContextCompat.getColor(context, R.color.colorLevel_2)
            in WIND_SPEED_5..WIND_SPEED_7 -> ContextCompat.getColor(context, R.color.colorLevel_3)
            in WIND_SPEED_7..WIND_SPEED_9 -> ContextCompat.getColor(context, R.color.colorLevel_4)
            in WIND_SPEED_9..WIND_SPEED_11 -> ContextCompat.getColor(context, R.color.colorLevel_5)
            in WIND_SPEED_11..Float.MAX_VALUE -> ContextCompat.getColor(context, R.color.colorLevel_6)
            else -> Color.TRANSPARENT
        }
    }

    fun getDirection(context: Context): String? {
        return when(degree) {
            null -> null
            in 0f..22.5f -> context.getString(R.string.wind_direction_short_N)
            in 22.5f..67.5f -> context.getString(R.string.wind_direction_short_NE)
            in 67.5f..112.5f -> context.getString(R.string.wind_direction_short_E)
            in 112.5f..157.5f -> context.getString(R.string.wind_direction_short_SE)
            in 157.5f..202.5f -> context.getString(R.string.wind_direction_short_S)
            in 202.5f..247.5f -> context.getString(R.string.wind_direction_short_SW)
            in 247.5f..292.5f -> context.getString(R.string.wind_direction_short_W)
            in 292.5f..337.5f -> context.getString(R.string.wind_direction_short_NW)
            in 337.5f..360f -> context.getString(R.string.wind_direction_short_N)
            else -> context.getString(R.string.wind_direction_short_variable)
        }
    }

    val arrow: String?
        get() = when(degree) {
            null -> null
            -1f -> "⟳"
            in 22.5..67.5 -> "↙"
            in 67.5..112.5 -> "←"
            in 112.5..157.5 -> "↖"
            in 157.5..202.5 -> "↑"
            in 202.5..247.5 -> "↗"
            in 247.5..292.5 -> "→"
            in 292.5..337.5 -> "↘"
            else -> "↓"
        }

    fun getStrength(context: Context): String? {
        return when (speed) {
            null -> null
            in 0f..WIND_SPEED_0 -> context.getString(R.string.wind_strength_0)
            in WIND_SPEED_0..WIND_SPEED_1 -> context.getString(R.string.wind_strength_1)
            in WIND_SPEED_1..WIND_SPEED_2 -> context.getString(R.string.wind_strength_2)
            in WIND_SPEED_2..WIND_SPEED_3 -> context.getString(R.string.wind_strength_3)
            in WIND_SPEED_3..WIND_SPEED_4 -> context.getString(R.string.wind_strength_4)
            in WIND_SPEED_4..WIND_SPEED_5 -> context.getString(R.string.wind_strength_5)
            in WIND_SPEED_5..WIND_SPEED_6 -> context.getString(R.string.wind_strength_6)
            in WIND_SPEED_6..WIND_SPEED_7 -> context.getString(R.string.wind_strength_7)
            in WIND_SPEED_7..WIND_SPEED_8 -> context.getString(R.string.wind_strength_8)
            in WIND_SPEED_8..WIND_SPEED_9 -> context.getString(R.string.wind_strength_9)
            in WIND_SPEED_9..WIND_SPEED_10 -> context.getString(R.string.wind_strength_10)
            in WIND_SPEED_10..WIND_SPEED_11 -> context.getString(R.string.wind_strength_11)
            in WIND_SPEED_11..Float.MAX_VALUE -> context.getString(R.string.wind_strength_12)
            else -> null
        }
    }

    fun getShortDescription(context: Context, unit: SpeedUnit): String {
        val builder = StringBuilder()
        arrow?.let {
            builder.append(it)
        }
        speed?.let {
            if (builder.toString().isNotEmpty()) builder.append(" ")
            builder.append(unit.getValueText(context, it))
        }
        return builder.toString()
    }

    fun getDescription(context: Context, unit: SpeedUnit): String {
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

    companion object {
        const val WIND_SPEED_0 = 2f
        const val WIND_SPEED_1 = 6f
        const val WIND_SPEED_2 = 12f
        const val WIND_SPEED_3 = 19f
        const val WIND_SPEED_4 = 30f
        const val WIND_SPEED_5 = 40f
        const val WIND_SPEED_6 = 51f
        const val WIND_SPEED_7 = 62f
        const val WIND_SPEED_8 = 75f
        const val WIND_SPEED_9 = 87f
        const val WIND_SPEED_10 = 103f
        const val WIND_SPEED_11 = 117f
    }
}
