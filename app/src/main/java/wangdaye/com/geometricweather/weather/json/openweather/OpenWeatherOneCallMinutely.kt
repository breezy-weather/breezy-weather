package wangdaye.com.geometricweather.weather.json.openweather

import kotlinx.serialization.Serializable

@Serializable
data class OpenWeatherOneCallMinutely(
    val dt: Long,
    val precipitation: Float?
)
