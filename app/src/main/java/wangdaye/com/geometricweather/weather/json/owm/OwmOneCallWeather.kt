package wangdaye.com.geometricweather.weather.json.owm

import kotlinx.serialization.Serializable

@Serializable
data class OwmOneCallWeather(
    val id: Int?,
    val main: String?,
    val description: String?,
    val icon: String?
)
