package wangdaye.com.geometricweather.weather.json.owm

import kotlinx.serialization.Serializable

/**
 * OpenWeather One Call result.
 */
@Serializable
data class OwmOneCallResult(
    val lat: Float?,
    val lon: Float?,
    val timezone: String?,
    val current: OwmOneCallCurrent?,
    val minutely: List<OwmOneCallMinutely>?,
    val hourly: List<OwmOneCallHourly>?,
    val daily: List<OwmOneCallDaily>?,
    val alerts: List<OwmOneCallAlert>?
)