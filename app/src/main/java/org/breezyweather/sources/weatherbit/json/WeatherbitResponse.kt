package org.breezyweather.sources.weatherbit.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherbitResponse<T>(
    val data: List<T>?
)
