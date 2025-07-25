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
import breezyweather.domain.weather.model.Precipitation
import org.breezyweather.R

@ColorInt
fun Precipitation.getHalfDayPrecipitationColor(context: Context): Int {
    return if (total == null) {
        Color.TRANSPARENT
    } else {
        when (total!!) {
            in 0.0..Precipitation.PRECIPITATION_HALF_DAY_LIGHT ->
                ContextCompat.getColor(context, R.color.colorLevel_1)
            in Precipitation.PRECIPITATION_HALF_DAY_LIGHT..Precipitation.PRECIPITATION_HALF_DAY_MEDIUM ->
                ContextCompat.getColor(context, R.color.colorLevel_2)
            in Precipitation.PRECIPITATION_HALF_DAY_MEDIUM..Precipitation.PRECIPITATION_HALF_DAY_HEAVY ->
                ContextCompat.getColor(context, R.color.colorLevel_3)
            in Precipitation.PRECIPITATION_HALF_DAY_HEAVY..Precipitation.PRECIPITATION_HALF_DAY_RAINSTORM ->
                ContextCompat.getColor(context, R.color.colorLevel_4)
            in Precipitation.PRECIPITATION_HALF_DAY_RAINSTORM..Double.MAX_VALUE ->
                ContextCompat.getColor(context, R.color.colorLevel_5)
            else -> Color.TRANSPARENT
        }
    }
}

@ColorInt
fun Precipitation.getHourlyPrecipitationColor(context: Context): Int {
    return if (total == null) {
        Color.TRANSPARENT
    } else {
        when (total!!) {
            in 0.0..Precipitation.PRECIPITATION_HOURLY_LIGHT ->
                ContextCompat.getColor(context, R.color.colorLevel_1)
            in Precipitation.PRECIPITATION_HOURLY_LIGHT..Precipitation.PRECIPITATION_HOURLY_MEDIUM ->
                ContextCompat.getColor(context, R.color.colorLevel_2)
            in Precipitation.PRECIPITATION_HOURLY_MEDIUM..Precipitation.PRECIPITATION_HOURLY_HEAVY ->
                ContextCompat.getColor(context, R.color.colorLevel_3)
            in Precipitation.PRECIPITATION_HOURLY_HEAVY..Precipitation.PRECIPITATION_HOURLY_RAINSTORM ->
                ContextCompat.getColor(context, R.color.colorLevel_4)
            in Precipitation.PRECIPITATION_HOURLY_RAINSTORM..Double.MAX_VALUE ->
                ContextCompat.getColor(context, R.color.colorLevel_5)
            else -> Color.TRANSPARENT
        }
    }
}
