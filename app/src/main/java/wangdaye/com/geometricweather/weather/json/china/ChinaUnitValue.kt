package wangdaye.com.geometricweather.weather.json.china

import kotlinx.serialization.Serializable

@Serializable
data class ChinaUnitValue(
    val unit: String?,
    val value: String?
)
