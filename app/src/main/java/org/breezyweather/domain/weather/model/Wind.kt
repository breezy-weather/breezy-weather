/*
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

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

fun Wind.getDirection(context: Context, short: Boolean = true): String? {
    if (degree == null) return null
    return when (degree!!) {
        in 0.0..22.5 -> context.getString(
            if (short) R.string.wind_direction_N_short else R.string.wind_direction_N
        )
        in 22.5..67.5 -> context.getString(
            if (short) R.string.wind_direction_NE_short else R.string.wind_direction_NE
        )
        in 67.5..112.5 -> context.getString(
            if (short) R.string.wind_direction_E_short else R.string.wind_direction_E
        )
        in 112.5..157.5 -> context.getString(
            if (short) R.string.wind_direction_SE_short else R.string.wind_direction_SE
        )
        in 157.5..202.5 -> context.getString(
            if (short) R.string.wind_direction_S_short else R.string.wind_direction_S
        )
        in 202.5..247.5 -> context.getString(
            if (short) R.string.wind_direction_SW_short else R.string.wind_direction_SW
        )
        in 247.5..292.5 -> context.getString(
            if (short) R.string.wind_direction_W_short else R.string.wind_direction_W
        )
        in 292.5..337.5 -> context.getString(
            if (short) R.string.wind_direction_NW_short else R.string.wind_direction_NW
        )
        in 337.5..360.0 -> context.getString(
            if (short) R.string.wind_direction_N_short else R.string.wind_direction_N
        )
        else -> context.getString(R.string.wind_direction_variable)
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

fun Wind.getContentDescription(context: Context, unit: SpeedUnit): String {
    val builder = StringBuilder()
    speed?.let {
        builder.append(context.getString(R.string.wind_speed))
        builder.append(context.getString(R.string.colon_separator))
        builder.append(unit.getValueVoice(context, it))
        if (!getStrength(context).isNullOrEmpty()) {
            builder.append(context.getString(R.string.comma_separator))
            builder.append(getStrength(context))
        }
    }
    if (!getDirection(context).isNullOrEmpty()) {
        if (builder.toString().isNotEmpty()) builder.append(context.getString(R.string.comma_separator))
        builder.append(context.getString(R.string.wind_direction))
        builder.append(context.getString(R.string.colon_separator))
        builder.append(getDirection(context, short = false))
    }
    return builder.toString()
}
