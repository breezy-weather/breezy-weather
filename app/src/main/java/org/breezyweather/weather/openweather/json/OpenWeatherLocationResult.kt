package org.breezyweather.weather.openweather.json

import kotlinx.serialization.Serializable

/**
 * OpenWeather location result.
 */
@Serializable
data class OpenWeatherLocationResult(
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String
)
