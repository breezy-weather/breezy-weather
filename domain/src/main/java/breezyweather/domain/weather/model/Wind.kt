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

package breezyweather.domain.weather.model

import java.io.Serializable

/**
 * DailyWind.
 *
 * default unit:
 * [.speed] : [SpeedUnit.MPS]
 * [.gust] : [SpeedUnit.MPS]
 */
data class Wind(
    /**
     * Between 0 and 360, or -1 if variable
     */
    val degree: Double? = null,
    /**
     * In m/s
     */
    val speed: Double? = null,
    /**
     * In m/s
     */
    val gusts: Double? = null,
) : Serializable {

    val isValid: Boolean
        get() = speed != null && speed > 0

    val arrow: String?
        get() = when (degree) {
            null -> null
            -1.0 -> "⟳"
            in 22.5..67.5 -> "↙"
            in 67.5..112.5 -> "←"
            in 112.5..157.5 -> "↖"
            in 157.5..202.5 -> "↑"
            in 202.5..247.5 -> "↗"
            in 247.5..292.5 -> "→"
            in 292.5..337.5 -> "↘"
            else -> "↓"
        }

    companion object {
        const val WIND_SPEED_0 = 0.5
        const val WIND_SPEED_1 = 1.5
        const val WIND_SPEED_2 = 3.3
        const val WIND_SPEED_3 = 5.5
        const val WIND_SPEED_4 = 7.9
        const val WIND_SPEED_5 = 10.7
        const val WIND_SPEED_6 = 13.8
        const val WIND_SPEED_7 = 17.1
        const val WIND_SPEED_8 = 20.7
        const val WIND_SPEED_9 = 24.4
        const val WIND_SPEED_10 = 28.4
        const val WIND_SPEED_11 = 32.6
    }
}
