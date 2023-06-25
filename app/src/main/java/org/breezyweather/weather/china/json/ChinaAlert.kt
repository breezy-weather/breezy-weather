package org.breezyweather.weather.china.json

import java.util.*

import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer

@Serializable
data class ChinaAlert(
    val locationKey: String?,
    val level: String?,
    @Serializable(DateSerializer::class) val pubTime: Date?,
    val alertId: String?,
    val detail: String?,
    val title: String?,
    val type: String?
)
