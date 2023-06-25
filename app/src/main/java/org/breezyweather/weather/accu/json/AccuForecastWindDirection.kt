package org.breezyweather.weather.accu.json

import kotlinx.serialization.Serializable

@Serializable
data class AccuForecastWindDirection(
    val Degrees: Int?,
    val Localized: String?
)
