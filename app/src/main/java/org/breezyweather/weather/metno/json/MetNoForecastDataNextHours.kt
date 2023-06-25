package org.breezyweather.weather.metno.json

import kotlinx.serialization.Serializable

@Serializable
data class MetNoForecastDataNextHours(
    val summary: MetNoForecastDataSummary?,
    val details: MetNoForecastDataDetails?
)
