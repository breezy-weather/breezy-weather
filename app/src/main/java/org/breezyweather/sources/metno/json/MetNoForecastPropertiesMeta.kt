package org.breezyweather.sources.metno.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import java.util.*

@Serializable
data class MetNoForecastPropertiesMeta(
    @Serializable(DateSerializer::class) @SerialName("updated_at") val updatedAt: Date?
)
