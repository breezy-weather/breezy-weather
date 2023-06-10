package wangdaye.com.geometricweather.weather.json.owm

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OwmOneCallPrecipitation(
    @SerialName("1h") val cumul1h: Float?
)
