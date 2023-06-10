package wangdaye.com.geometricweather.weather.json.owm

import kotlinx.serialization.Serializable

@Serializable
data class OwmOneCallMinutely(
    val dt: Long,
    val precipitation: Int?
)
