package org.breezyweather.weather.openweather.json

import kotlinx.serialization.Serializable

@Serializable
data class OpenWeatherAirPollution(
    val dt: Long,
    val components: OpenWeatherAirPollutionComponents?,
)
