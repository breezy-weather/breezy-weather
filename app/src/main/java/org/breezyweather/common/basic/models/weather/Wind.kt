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
    val direction: String? = null,
    val degree: WindDegree? = null,
    val speed: Float? = null,
    val level: String? = null
) : Serializable {

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

    @ColorInt
    fun getWindColor(context: Context): Int {
        return if (speed == null) {
            Color.TRANSPARENT
        } else when (speed) {
            in 0f..WIND_SPEED_3 -> ContextCompat.getColor(context, R.color.colorLevel_1)
            in WIND_SPEED_3..WIND_SPEED_5 -> ContextCompat.getColor(context, R.color.colorLevel_2)
            in WIND_SPEED_5..WIND_SPEED_7 -> ContextCompat.getColor(context, R.color.colorLevel_3)
            in WIND_SPEED_7..WIND_SPEED_9 -> ContextCompat.getColor(context, R.color.colorLevel_4)
            in WIND_SPEED_9..WIND_SPEED_11 -> ContextCompat.getColor(context, R.color.colorLevel_5)
            in WIND_SPEED_11..Float.MAX_VALUE -> ContextCompat.getColor(context, R.color.colorLevel_6)
            else -> Color.TRANSPARENT
        }
    }

    fun getShortWindDescription(context: Context, unit: SpeedUnit): String {
        val builder = StringBuilder()
        degree?.windArrow?.let {
            builder.append(it)
        }
        speed?.let {
            if (builder.toString().isNotEmpty()) builder.append(" ")
            builder.append(unit.getValueText(context, it))
        }
        return builder.toString()
    }

    fun getWindDescription(context: Context, unit: SpeedUnit): String {
        val builder = StringBuilder()
        if (!direction.isNullOrEmpty()) {
            builder.append(direction)
        }
        speed?.let {
            if (builder.toString().isNotEmpty()) builder.append(" ")
            builder.append(unit.getValueText(context, it))
        }
        if (!level.isNullOrEmpty()) {
            if (builder.toString().isNotEmpty()) builder.append(" ")
            builder.append("(").append(level).append(")")
        }
        degree?.windArrow?.let {
            if (builder.toString().isNotEmpty()) builder.append(" ")
            builder.append(it)
        }
        return builder.toString()
    }

    val isValidSpeed: Boolean
        get() = speed != null && speed > 0
}
