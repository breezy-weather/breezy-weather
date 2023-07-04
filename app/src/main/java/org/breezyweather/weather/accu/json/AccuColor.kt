package org.breezyweather.weather.accu.json

import kotlinx.serialization.Serializable

@Serializable
data class AccuColor(
    val Red: Int,
    val Green: Int,
    val Blue: Int
)
