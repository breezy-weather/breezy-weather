package org.breezyweather.sources.weatherbit.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * I opted in to use alerts from current weather report
 * They are exactly the same as /alerts, but save 1 api call
 */
@Serializable
data class WeatherbitCurrentResponse(
    @SerialName("data") val current: List<WeatherbitCurrent>?,
    val alerts: List<WeatherbitAlert>?
)