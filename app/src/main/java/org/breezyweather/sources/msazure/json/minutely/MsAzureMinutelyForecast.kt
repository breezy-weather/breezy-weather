package org.breezyweather.sources.msazure.json.minutely

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import java.util.Date

@Serializable
data class MsAzureMinutelyForecast(
    @Serializable(DateSerializer::class) val startTime: Date,
    val minute: Int?,
    val dbz: Double?
)