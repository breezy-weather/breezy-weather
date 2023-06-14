package org.breezyweather.weather.json.accu

import kotlinx.serialization.Serializable

@Serializable
data class AccuCurrentWindDirection(
    val Degrees: Int,
    val Localized: String?
)
