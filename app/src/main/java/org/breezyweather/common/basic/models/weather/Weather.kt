package org.breezyweather.common.basic.models.weather

import java.io.Serializable
import java.util.*

class Weather(
    val base: Base,
    val current: Current? = null,
    var yesterday: History? = null,
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
}
