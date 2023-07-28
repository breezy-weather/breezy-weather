package org.breezyweather.sources.here.json

import kotlinx.serialization.Serializable

@Serializable
data class HereWeatherForecasts<T>(
    val forecasts: List<T>?
)
