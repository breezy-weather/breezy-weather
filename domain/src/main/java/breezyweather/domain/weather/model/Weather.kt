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

import breezyweather.domain.weather.wrappers.AirQualityWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.PollenWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import java.io.Serializable
import java.util.Date
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

data class Weather(
    val base: Base = Base(),
    val current: Current? = null,
    val normals: Normals? = null,
    val dailyForecast: List<Daily> = emptyList(),
    val hourlyForecast: List<Hourly> = emptyList(),
    val minutelyForecast: List<Minutely> = emptyList(),
    val alertList: List<Alert> = emptyList(),
) : Serializable {

    // Only hourly in the future, starting from current hour
    val nextHourlyForecast = hourlyForecast.filter {
        // Example: 15:01 -> starts at 15:00, 15:59 -> starts at 15:00
        it.date.time >= System.currentTimeMillis() - 1.hours.inWholeMilliseconds
    }

    val todayIndex = dailyForecast.indexOfFirst {
        it.date.time > Date().time - 1.days.inWholeMilliseconds
    }
    val today
        get() = dailyForecast.getOrNull(todayIndex)
    val tomorrow
        get() = dailyForecast.firstOrNull {
            it.date.time > Date().time
        }

    val dailyForecastStartingToday = if (todayIndex >= 0) {
        dailyForecast.subList(todayIndex, dailyForecast.size)
    } else {
        emptyList()
    }

    fun isValid(pollingIntervalHours: Float?): Boolean {
        val updateTime = base.refreshTime?.time ?: 0
        val currentTime = System.currentTimeMillis()
        return pollingIntervalHours == null ||
            (currentTime >= updateTime && currentTime - updateTime < pollingIntervalHours * 1.hours.inWholeMilliseconds)
    }

    val currentAlertList: List<Alert> = alertList
        .filter {
            (it.startDate == null && it.endDate == null) ||
                (it.startDate != null && it.endDate != null && Date() in it.startDate..it.endDate) ||
                (it.startDate == null && it.endDate != null && Date() < it.endDate) ||
                (it.startDate != null && it.endDate == null && Date() > it.startDate)
        }

    val minutelyForecastBy5Minutes: List<Minutely>
        get() {
            return if (minutelyForecast.any { it.minuteInterval != 5 }) {
                val newMinutelyList = mutableListOf<Minutely>()

                if (minutelyForecast.any { it.minuteInterval == 1 }) {
                    // Let’s assume 1-minute by 1-minute forecast are always 1-minute all along
                    for (i in minutelyForecast.indices step 5) {
                        newMinutelyList.add(
                            minutelyForecast[i].copy(
                                precipitationIntensity = doubleArrayOf(
                                    minutelyForecast[i].precipitationIntensity ?: 0.0,
                                    minutelyForecast.getOrNull(i + 1)?.precipitationIntensity ?: 0.0,
                                    minutelyForecast.getOrNull(i + 2)?.precipitationIntensity ?: 0.0,
                                    minutelyForecast.getOrNull(i + 3)?.precipitationIntensity ?: 0.0,
                                    minutelyForecast.getOrNull(i + 4)?.precipitationIntensity ?: 0.0
                                ).average(),
                                minuteInterval = 5
                            )
                        )
                    }
                } else {
                    // Let’s assume the other cases are divisible by 5
                    for (i in minutelyForecast.indices) {
                        if (minutelyForecast[i].minuteInterval == 5) {
                            newMinutelyList.add(minutelyForecast[i])
                        } else {
                            for (j in 0..<minutelyForecast[i].minuteInterval.div(5)) {
                                newMinutelyList.add(
                                    minutelyForecast[i].copy(
                                        date = Date(
                                            minutelyForecast[i].date.time + (j.times(5)).minutes.inWholeMilliseconds
                                        ),
                                        minuteInterval = 5
                                    )
                                )
                            }
                        }
                    }
                }
                return newMinutelyList
            } else {
                minutelyForecast
            }
        }

    fun toWeatherWrapper() = WeatherWrapper(
        current = this.current?.toCurrentWrapper(),
        normals = this.normals,
        dailyForecast = this.dailyForecast.map { it.toDailyWrapper() },
        hourlyForecast = this.hourlyForecast.map { it.toHourlyWrapper() },
        minutelyForecast = this.minutelyForecast,
        alertList = this.alertList
    )

    fun toDailyWrapperList(startDate: Date): List<DailyWrapper> {
        return this.dailyForecast
            .filter { it.date >= startDate }
            .map { it.toDailyWrapper() }
    }

    fun toHourlyWrapperList(startDate: Date): List<HourlyWrapper> {
        return this.hourlyForecast
            .filter { it.date >= startDate }
            .map { it.toHourlyWrapper() }
    }

    fun toAirQualityWrapperList(startDate: Date): AirQualityWrapper? {
        val hourlyAirQuality = hourlyForecast.filter { it.date >= startDate && it.airQuality?.isValid == true }
        val dailyAirQuality = dailyForecast.filter { it.date >= startDate && it.airQuality?.isValid == true }
        val currentAirQuality = current?.airQuality
        if (hourlyAirQuality.isEmpty() && dailyAirQuality.isEmpty()) return null

        return AirQualityWrapper(
            current = currentAirQuality,
            dailyForecast = dailyAirQuality.associate { it.date to it.airQuality!! },
            hourlyForecast = hourlyAirQuality.associate { it.date to it.airQuality!! }
        )
    }

    fun toPollenWrapperList(startDate: Date): PollenWrapper? {
        val dailyPollen = dailyForecast.filter { it.date >= startDate && it.pollen?.isValid == true }
        if (dailyPollen.isEmpty()) return null

        return PollenWrapper(
            dailyForecast = dailyPollen.associate { it.date to it.pollen!! }
        )
    }

    fun toMinutelyWrapper(): List<Minutely>? {
        val now = Date()
        return minutelyForecast
            .filter { it.date >= now }
            .let { if (it.size > 3) it else null }
    }

    fun toAlertsWrapper(): List<Alert> {
        val now = Date()
        return alertList.filter {
            it.endDate == null || it.endDate.time > now.time
        }
    }
}
