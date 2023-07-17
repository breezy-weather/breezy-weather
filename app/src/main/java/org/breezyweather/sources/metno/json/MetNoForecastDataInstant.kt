package org.breezyweather.sources.metno.json

import kotlinx.serialization.Serializable

@Serializable
data class MetNoForecastDataInstant(
    val details: MetNoForecastDataDetails?
)
