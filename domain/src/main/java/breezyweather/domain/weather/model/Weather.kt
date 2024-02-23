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

import breezyweather.domain.weather.wrappers.WeatherWrapper
import java.io.Serializable
import java.util.Date

data class Weather(
    val base: Base = Base(),
    val current: Current? = null,
    val normals: Normals? = null,
    val dailyForecast: List<Daily> = emptyList(),
    val hourlyForecast: List<Hourly> = emptyList(),
    val minutelyForecast: List<Minutely> = emptyList(),
    val alertList: List<Alert> = emptyList()
) : Serializable {

    // Only hourly in the future, starting from current hour
    val nextHourlyForecast = hourlyForecast.filter {
        // Example: 15:01 -> starts at 15:00, 15:59 -> starts at 15:00
        it.date.time >= System.currentTimeMillis() - (3600 * 1000)
    }

    val todayIndex = dailyForecast.indexOfFirst {
        it.date.time > Date().time - (24 * 3600 * 1000)
    }
    val today
        get() = dailyForecast.getOrNull(todayIndex)
    val tomorrow
        get() = dailyForecast.firstOrNull {
            it.date.time > Date().time
        }

    val dailyForecastStartingToday = if (todayIndex >= 0) {
        dailyForecast.subList(todayIndex, dailyForecast.size)
    } else emptyList()

    fun isValid(pollingIntervalHours: Float?): Boolean {
        val updateTime = base.refreshTime?.time ?: 0
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

    fun toWeatherWrapper() = WeatherWrapper(
        current = this.current,
        normals = this.normals,
        dailyForecast = this.dailyForecast,
        hourlyForecast = this.hourlyForecast.map { it.toHourlyWrapper() },
        minutelyForecast = this.minutelyForecast,
        alertList = this.alertList
    )
}
