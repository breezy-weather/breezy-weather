package wangdaye.com.geometricweather.weather.json.openweather

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenWeatherOneCallPrecipitation(
    @SerialName("1h") val cumul1h: Float?
)
