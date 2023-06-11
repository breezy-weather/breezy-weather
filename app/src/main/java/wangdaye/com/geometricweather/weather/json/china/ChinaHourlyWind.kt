package wangdaye.com.geometricweather.weather.json.china

import kotlinx.serialization.Serializable

@Serializable
data class ChinaHourlyWind(
    val value: List<ChinaHourlyWindValue>?
)
