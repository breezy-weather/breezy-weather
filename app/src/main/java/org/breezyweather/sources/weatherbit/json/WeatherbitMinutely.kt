package org.breezyweather.sources.weatherbit.json

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class WeatherbitMinutely(
    @SerialName("ts") val time: Long,
    @SerialName("precip") val precipitation: Float?,
    val snow: Float?
)