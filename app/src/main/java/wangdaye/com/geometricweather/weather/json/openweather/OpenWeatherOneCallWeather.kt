package wangdaye.com.geometricweather.weather.json.openweather

import kotlinx.serialization.Serializable

@Serializable
data class OpenWeatherOneCallWeather(
    val id: Int?,
    val main: String?,
    val description: String?,
    val icon: String?
)
