package org.breezyweather.weather.china.json

import java.util.*

import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer

@Serializable
data class ChinaMinutelyPrecipitation(
    @Serializable(DateSerializer::class) val pubTime: Date?,
    val weather: String?,
    val description: String?,
    val value: List<Double>?
)
