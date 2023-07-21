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
