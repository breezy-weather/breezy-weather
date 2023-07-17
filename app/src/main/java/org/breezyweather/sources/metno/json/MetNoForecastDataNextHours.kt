package org.breezyweather.sources.metno.json

import kotlinx.serialization.Serializable

@Serializable
data class MetNoForecastDataNextHours(
    val summary: MetNoForecastDataSummary?,
    val details: MetNoForecastDataDetails?
)
