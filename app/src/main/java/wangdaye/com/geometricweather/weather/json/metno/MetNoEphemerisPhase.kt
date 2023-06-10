package wangdaye.com.geometricweather.weather.json.metno

import kotlinx.serialization.Serializable
import wangdaye.com.geometricweather.common.serializer.DateSerializer
import java.util.*

@Serializable
data class MetNoEphemerisPhase(
    @Serializable(DateSerializer::class) val time: Date?
)
