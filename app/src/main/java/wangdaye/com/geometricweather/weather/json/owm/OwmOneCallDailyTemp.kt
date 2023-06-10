package wangdaye.com.geometricweather.weather.json.owm

import kotlinx.serialization.Serializable

@Serializable
data class OwmOneCallDailyTemp(
    val day: Float?,
    val min: Float?,
    val max: Float?,
    val night: Float?,
    val eve: Float?,
    val morn: Float?
)