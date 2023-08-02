package org.breezyweather.sources.msazure.json.alerts

import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import java.util.Date

@Serializable
data class MsAzureWeatherAlertArea(
    val name: String?,
    val summary: String?,
    @Serializable(DateSerializer::class) val startTime: Date,
    @Serializable(DateSerializer::class) val endTime: Date,
    val alertDetails: String?
)