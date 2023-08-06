/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
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

import java.io.Serializable
import java.util.*

data class Weather(
    val base: Base = Base(),
    val current: Current? = null,
    val yesterday: History? = null,
    val dailyForecast: List<Daily> = emptyList(),
    val hourlyForecast: List<Hourly> = emptyList(),
    val minutelyForecast: List<Minutely> = emptyList(),
    val alertList: List<Alert> = emptyList()
) : Serializable {

    fun isValid(pollingIntervalHours: Float?): Boolean {
        val updateTime = base.updateDate.time
        val currentTime = System.currentTimeMillis()
        return (pollingIntervalHours == null
                || (currentTime >= updateTime
                && currentTime - updateTime < pollingIntervalHours * 60 * 60 * 1000))
    }

    val currentAlertList: List<Alert> = alertList
        .filter {
            (it.startDate == null && it.endDate == null)
                    || (it.startDate != null && it.endDate != null && Date() in it.startDate..it.endDate)
                    || (it.startDate == null && it.endDate != null && Date() < it.endDate)
                    || (it.startDate != null && it.endDate == null && Date() > it.startDate)
        }

    val validAirQuality: AirQuality?
        get() = if (current?.airQuality != null && current.airQuality.isValid) {
            current.airQuality
        } else if (dailyForecast.getOrNull(0)?.airQuality != null &&
            dailyForecast[0].airQuality!!.isValid) {
            dailyForecast[0].airQuality
        } else null
}
