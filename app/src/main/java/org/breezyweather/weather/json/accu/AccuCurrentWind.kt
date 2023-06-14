package org.breezyweather.weather.json.accu

import kotlinx.serialization.Serializable

@Serializable
data class AccuCurrentWind(
    val Direction: AccuCurrentWindDirection?,
    val Speed: AccuValueContainer?
)
