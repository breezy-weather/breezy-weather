package wangdaye.com.geometricweather.weather.json.china

import java.util.*

import kotlinx.serialization.Serializable
import wangdaye.com.geometricweather.common.serializer.DateSerializer

@Serializable
data class ChinaAlert(
    val locationKey: String?,
    val level: String?,
    @Serializable(DateSerializer::class) val pubTime: Date?,
    val alertId: String?,
    val detail: String?,
    val title: String?,
    val type: String?
)
