package wangdaye.com.geometricweather.weather.json.china

import java.util.*

import kotlinx.serialization.Serializable
import wangdaye.com.geometricweather.common.serializer.DateSerializer

@Serializable
data class ChinaSunRiseSetValue(
    @Serializable(DateSerializer::class) val from: Date,
    @Serializable(DateSerializer::class) val to: Date
)
