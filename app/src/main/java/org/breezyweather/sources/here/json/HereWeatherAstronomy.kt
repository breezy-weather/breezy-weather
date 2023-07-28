package org.breezyweather.sources.here.json

import kotlinx.serialization.Serializable

@Serializable
data class HereWeatherAstronomy(
    val time: String?,
    val sunRise: String?,
    val sunSet: String?,
    val moonRise: String?,
    val moonSet: String?,
    val moonPhase: Float?
)
