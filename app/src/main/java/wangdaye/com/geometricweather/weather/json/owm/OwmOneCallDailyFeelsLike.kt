package wangdaye.com.geometricweather.weather.json.owm

import kotlinx.serialization.Serializable

@Serializable
data class OwmOneCallDailyFeelsLike(
    val day: Float?,
    val night: Float?,
    val eve: Float?,
    val morn: Float?
)