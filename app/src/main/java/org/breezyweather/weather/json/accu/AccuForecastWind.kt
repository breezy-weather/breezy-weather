package org.breezyweather.weather.json.accu

import kotlinx.serialization.Serializable

@Serializable
data class AccuForecastWind(
    val Speed: AccuValue?,
    val Direction: AccuForecastWindDirection?
)
