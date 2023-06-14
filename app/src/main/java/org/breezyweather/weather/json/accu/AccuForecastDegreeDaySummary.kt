package org.breezyweather.weather.json.accu

import kotlinx.serialization.Serializable

@Serializable
data class AccuForecastDegreeDaySummary(
    val Heating: AccuValue?,
    val Cooling: AccuValue?
)
