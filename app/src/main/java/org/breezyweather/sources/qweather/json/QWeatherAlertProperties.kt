package org.breezyweather.sources.qweather.json

import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import java.util.Date

@Serializable
data class QWeatherAlertProperties(
    val sender: String?,
    @Serializable(DateSerializer::class) val pubTime: Date,
    val title: String,
    val text: String,
    val id: String,
    @Serializable(DateSerializer::class) val startTime: Date,
    @Serializable(DateSerializer::class) val endTime: Date?,
)
