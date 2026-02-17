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

package breezyweather.domain.weather.model

import breezyweather.domain.weather.wrappers.DailyWrapper
import java.io.Serializable
import java.util.Date
import kotlin.time.Duration

/**
 * Daily.
 */
data class Daily(
    /**
     * Daily date initialized at 00:00 in the TimeZone of the location
     */
    val date: Date,
    val day: HalfDay? = null,
    val night: HalfDay? = null,
    val degreeDay: DegreeDay? = null,
    val sun: Astro? = null,
    val twilight: Astro? = null,
    val moon: Astro? = null,
    val moonPhase: MoonPhase? = null,
    val airQuality: AirQuality? = null,
    val pollen: Pollen? = null,
    val uV: UV? = null,
    val sunshineDuration: Duration? = null,
    val relativeHumidity: DailyRelativeHumidity? = null,
    val dewPoint: DailyDewPoint? = null,
    val pressure: DailyPressure? = null,
    val cloudCover: DailyCloudCover? = null,
    val visibility: DailyVisibility? = null,
) : Serializable {

    fun toDailyWrapper() = DailyWrapper(
        date = this.date,
        day = this.day?.toHalfDayWrapper(),
        night = this.night?.toHalfDayWrapper(),
        degreeDay = this.degreeDay,
        uV = this.uV,
        sunshineDuration = this.sunshineDuration
    )
}
