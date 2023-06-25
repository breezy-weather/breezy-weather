package org.breezyweather.weather.accu.json

import kotlinx.serialization.Serializable

@Serializable
data class AccuForecastDegreeDaySummary(
    val Heating: AccuValue?,
    val Cooling: AccuValue?
)
