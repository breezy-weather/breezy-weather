/**
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

package org.breezyweather.common.basic.models.weather

enum class WeatherCode(val id: String) {

    CLEAR("clear"),
    PARTLY_CLOUDY("partly_cloudy"),
    CLOUDY("cloudy"),
    RAIN("rain"),
    SNOW("snow"),
    WIND("wind"),
    FOG("fog"),
    HAZE("haze"),
    SLEET("sleet"),
    HAIL("hail"),
    THUNDER("thunder"),
    THUNDERSTORM("thunderstorm");

    companion object {

        fun getInstance(
            value: String
        ): WeatherCode = with (value) {
            when {
                equals("partly_cloudy", ignoreCase = true) -> PARTLY_CLOUDY
                equals("cloudy", ignoreCase = true) -> CLOUDY
                equals("rain", ignoreCase = true) -> RAIN
                equals("snow", ignoreCase = true) -> SNOW
                equals("wind", ignoreCase = true) -> WIND
                equals("fog", ignoreCase = true) -> FOG
                equals("haze", ignoreCase = true) -> HAZE
                equals("sleet", ignoreCase = true) -> SLEET
                equals("hail", ignoreCase = true) -> HAIL
                equals("thunderstorm", ignoreCase = true) -> THUNDERSTORM
                equals("thunder", ignoreCase = true) -> THUNDER
                else -> CLEAR
            }
        }
    }

    val isPrecipitation: Boolean
        get() = this == RAIN || this == SNOW || this == SLEET || this == HAIL || this == THUNDERSTORM

    val isRain: Boolean
        get() = this == RAIN || this == SLEET || this == THUNDERSTORM

    val isSnow: Boolean
        get() = this == SNOW || this == SLEET

    val isIce: Boolean
        get() = this == HAIL
}