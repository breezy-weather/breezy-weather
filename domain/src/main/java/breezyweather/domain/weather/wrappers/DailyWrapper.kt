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

package breezyweather.domain.weather.wrappers

import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.DegreeDay
import breezyweather.domain.weather.model.HalfDay
import breezyweather.domain.weather.model.Pollen
import breezyweather.domain.weather.model.UV
import java.util.Date

/**
 * Daily wrapper
 */
data class DailyWrapper(
    val date: Date,
    val day: HalfDay? = null,
    val night: HalfDay? = null,
    val degreeDay: DegreeDay? = null,
    val uV: UV? = null,
    /**
     * Sunshine duration in hours (ex: 0.5 means 30 min)
     */
    val sunshineDuration: Double? = null,
) {
    fun toDaily(
        airQuality: AirQuality? = null,
        pollen: Pollen? = null,
    ) = Daily(
        date = this.date,
        day = this.day,
        night = this.night,
        degreeDay = this.degreeDay,
        airQuality = airQuality,
        pollen = pollen,
        uV = this.uV,
        sunshineDuration = this.sunshineDuration
    )
}
