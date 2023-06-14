package org.breezyweather.weather.json.openweather

import kotlinx.serialization.Serializable

@Serializable
data class OpenWeatherAirPollution(
    val dt: Long,
    val components: OpenWeatherAirPollutionComponents?,
)
