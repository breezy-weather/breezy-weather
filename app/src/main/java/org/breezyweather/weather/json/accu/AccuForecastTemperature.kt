package org.breezyweather.weather.json.accu

import kotlinx.serialization.Serializable

@Serializable
data class AccuForecastTemperature(
    val Minimum: AccuValue?,
    val Maximum: AccuValue?
)
