package org.breezyweather.sources.openweather.json

import kotlinx.serialization.Serializable

@Serializable
data class OpenWeatherOneCallMinutely(
    val dt: Long,
    val precipitation: Float?
)
