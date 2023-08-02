package org.breezyweather.sources.msazure.json.alerts

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import java.util.Date

@Serializable
data class MsAzureWeatherAlertArea(
    val name: String?,
    val summary: String?,
    @Serializable(DateSerializer::class) @SerialName("startTime") val start: Date,
    @Serializable(DateSerializer::class) @SerialName("endTime") val end: Date,
    val alertDetails: String?
)