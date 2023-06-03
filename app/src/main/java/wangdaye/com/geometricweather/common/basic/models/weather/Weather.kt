package wangdaye.com.geometricweather.common.basic.models.weather

import java.io.Serializable

class Weather(
    val base: Base,
    val current: Current? = null,
    var yesterday: History? = null,
    val dailyForecast: List<Daily> = arrayListOf(),
    val hourlyForecast: List<Hourly> = arrayListOf(),
    val minutelyForecast: List<Minutely> = arrayListOf(),
    val alertList: List<Alert> = arrayListOf()
) : Serializable {

    fun isValid(pollingIntervalHours: Float?): Boolean {
        val updateTime = base.updateDate.time
        val currentTime = System.currentTimeMillis()
        return (pollingIntervalHours == null
                || (currentTime >= updateTime
                && currentTime - updateTime < pollingIntervalHours * 60 * 60 * 1000))
    }
}
