package org.breezyweather.sources.accu.json

import kotlinx.serialization.Serializable

@Serializable
data class AccuAirQualityResult(
    val data: List<AccuAirQualityData>? = null
)
