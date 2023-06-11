package wangdaye.com.geometricweather.weather.json.china

import java.util.*

import kotlinx.serialization.Serializable
import wangdaye.com.geometricweather.common.serializer.DateSerializer

@Serializable
data class ChinaMinutelyPrecipitation(
    @Serializable(DateSerializer::class) val pubTime: Date?,
    val weather: String?,
    val description: String?,
    val value: List<Double>?
)
