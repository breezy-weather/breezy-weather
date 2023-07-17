package org.breezyweather.sources.openweather.json

import kotlinx.serialization.Serializable

/**
 * OpenWeather Air Pollution result.
 */
@Serializable
data class OpenWeatherAirPollutionResult(
    val list: List<OpenWeatherAirPollution>? = null
)
