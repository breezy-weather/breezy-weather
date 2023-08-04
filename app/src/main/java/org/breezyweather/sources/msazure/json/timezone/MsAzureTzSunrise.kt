package org.breezyweather.sources.msazure.json.timezone

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import java.util.Date

@Serializable
data class MsAzureTzSunrise(
    @Serializable(DateSerializer::class) @SerialName("WallTime") val time: Date,
    @Serializable(DateSerializer::class) @SerialName("Sunrise") val sunrise: Date,
    @Serializable(DateSerializer::class) @SerialName("Sunset") val sunset: Date
)