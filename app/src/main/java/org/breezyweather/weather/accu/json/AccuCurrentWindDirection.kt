package org.breezyweather.weather.accu.json

import kotlinx.serialization.Serializable

@Serializable
data class AccuCurrentWindDirection(
    val Degrees: Int,
    val Localized: String?
)
