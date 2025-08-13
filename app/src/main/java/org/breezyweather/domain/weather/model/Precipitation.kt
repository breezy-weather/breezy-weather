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
import org.breezyweather.unit.precipitation.Precipitation.Companion.millimeters

@ColorInt
fun Precipitation.getHalfDayPrecipitationColor(context: Context): Int {
    return if (total == null) {
        Color.TRANSPARENT
    } else {
        with(total!!) {
            when {
                this in 0.0.millimeters..Precipitation.PRECIPITATION_HALF_DAY_LIGHT.millimeters -> {
                    ContextCompat.getColor(context, R.color.colorLevel_1)
                }

                this in Precipitation.PRECIPITATION_HALF_DAY_LIGHT.millimeters
                    .rangeTo(Precipitation.PRECIPITATION_HALF_DAY_MEDIUM.millimeters) ->
                    ContextCompat.getColor(context, R.color.colorLevel_2)

                this in Precipitation.PRECIPITATION_HALF_DAY_MEDIUM.millimeters
                    .rangeTo(Precipitation.PRECIPITATION_HALF_DAY_HEAVY.millimeters) -> {
                    ContextCompat.getColor(context, R.color.colorLevel_3)
                }

                this in Precipitation.PRECIPITATION_HALF_DAY_HEAVY.millimeters
                    .rangeTo(Precipitation.PRECIPITATION_HALF_DAY_RAINSTORM.millimeters) -> {
                    ContextCompat.getColor(context, R.color.colorLevel_4)
                }

                this >= Precipitation.PRECIPITATION_HALF_DAY_RAINSTORM.millimeters -> {
                    ContextCompat.getColor(context, R.color.colorLevel_5)
                }

                else -> Color.TRANSPARENT
            }
        }
    }
}

@ColorInt
fun Precipitation.getHourlyPrecipitationColor(context: Context): Int {
    return if (total == null) {
        Color.TRANSPARENT
    } else {
        with(total!!) {
            when {
                this in 0.0.millimeters..Precipitation.PRECIPITATION_HOURLY_LIGHT.millimeters -> {
                    ContextCompat.getColor(context, R.color.colorLevel_1)
                }

                this in Precipitation.PRECIPITATION_HOURLY_LIGHT.millimeters
                    .rangeTo(Precipitation.PRECIPITATION_HOURLY_MEDIUM.millimeters) -> {
                    ContextCompat.getColor(context, R.color.colorLevel_2)
                }

                this in Precipitation.PRECIPITATION_HOURLY_MEDIUM.millimeters
                    .rangeTo(Precipitation.PRECIPITATION_HOURLY_HEAVY.millimeters) -> {
                    ContextCompat.getColor(context, R.color.colorLevel_3)
                }

                this in Precipitation.PRECIPITATION_HOURLY_HEAVY.millimeters
                    .rangeTo(Precipitation.PRECIPITATION_HOURLY_RAINSTORM.millimeters) -> {
                    ContextCompat.getColor(context, R.color.colorLevel_4)
                }

                this >= Precipitation.PRECIPITATION_HOURLY_RAINSTORM.millimeters -> {
                    ContextCompat.getColor(context, R.color.colorLevel_5)
                }

                else -> Color.TRANSPARENT
            }
        }
    }
}
