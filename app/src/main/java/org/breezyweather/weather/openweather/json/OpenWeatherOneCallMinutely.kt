package org.breezyweather.weather.openweather.json

import kotlinx.serialization.Serializable

@Serializable
data class OpenWeatherOneCallMinutely(
    val dt: Long,
    val precipitation: Float?
)
