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
import org.breezyweather.common.extensions.formatMeasure
import org.breezyweather.common.extensions.getBeaufortScaleStrength
import org.breezyweather.unit.formatting.UnitWidth
import org.breezyweather.unit.speed.Speed.Companion.centimetersPerSecond

fun Wind.validate(): Wind {
    return copy(
        degree = Wind.validateDegree(degree),
        speed = speed?.toValidOrNull(),
        gusts = gusts?.toValidOrNull()
    )
}

@ColorInt
fun Wind.getColor(context: Context): Int {
    if (speed == null) return Color.TRANSPARENT
    return when (speed!!.inBeaufort) {
        in 0..<4 -> ContextCompat.getColor(context, R.color.colorLevel_1)
        in 4..<6 -> ContextCompat.getColor(context, R.color.colorLevel_2)
        in 6..<8 -> ContextCompat.getColor(context, R.color.colorLevel_3)
        in 8..<10 -> ContextCompat.getColor(context, R.color.colorLevel_4)
        in 10..<12 -> ContextCompat.getColor(context, R.color.colorLevel_5)
        in 12..Integer.MAX_VALUE -> ContextCompat.getColor(context, R.color.colorLevel_6)
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
    return speed?.getBeaufortScaleStrength(context)
}

fun Wind.getShortDescription(context: Context): String? {
    val builder = StringBuilder()
    arrow?.let {
        builder.append(it)
    }
    speed?.let {
        if (builder.toString().isNotEmpty()) builder.append(" ")
        builder.append(it.formatMeasure(context))
    }
    return builder.toString().ifEmpty { null }
}

fun Wind.getContentDescription(
    context: Context,
    withGusts: Boolean = false,
): String {
    val builder = StringBuilder()
    speed?.let {
        builder.append(it.formatMeasure(context, unitWidth = UnitWidth.LONG))
        if (!getStrength(context).isNullOrEmpty()) {
            builder.append(context.getString(org.breezyweather.unit.R.string.locale_separator))
            builder.append(getStrength(context))
        }
    }
    if (!getDirection(context).isNullOrEmpty()) {
        if (builder.toString().isNotEmpty()) {
            builder.append(context.getString(org.breezyweather.unit.R.string.locale_separator))
        }
        if (degree!! in 0.0..360.0) {
            builder.append(context.getString(R.string.wind_origin, getDirection(context, short = false)))
        } else {
            builder.append(getDirection(context, short = false))
        }
    }
    if (withGusts) {
        gusts?.let {
            if (it > (speed ?: 0.centimetersPerSecond)) {
                if (builder.toString().isNotEmpty()) {
                    builder.append(context.getString(org.breezyweather.unit.R.string.locale_separator))
                }
                builder.append(context.getString(R.string.wind_gusts_short))
                builder.append(context.getString(R.string.colon_separator))
                builder.append(it.formatMeasure(context, unitWidth = UnitWidth.LONG))
            }
        }
    }
    return builder.toString()
}
