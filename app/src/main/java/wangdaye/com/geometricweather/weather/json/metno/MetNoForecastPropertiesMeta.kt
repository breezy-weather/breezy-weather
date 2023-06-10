package wangdaye.com.geometricweather.weather.json.metno

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import wangdaye.com.geometricweather.common.serializer.DateSerializer
import java.util.*

@Serializable
data class MetNoForecastPropertiesMeta(
    @Serializable(DateSerializer::class) @SerialName("updated_at") val updatedAt: Date?
)
