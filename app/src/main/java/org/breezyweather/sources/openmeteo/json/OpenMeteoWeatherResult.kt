package org.breezyweather.sources.openmeteo.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Open-Meteo weather
 */
@Serializable
data class OpenMeteoWeatherResult(
    @SerialName("current_weather") val currentWeather: OpenMeteoWeatherCurrent?,
    val daily: OpenMeteoWeatherDaily?,
    val hourly: OpenMeteoWeatherHourly?
)
