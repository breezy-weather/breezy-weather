package org.breezyweather.weather.accu.json

import kotlinx.serialization.Serializable

@Serializable
data class AccuAirQualityResult(
    val data: List<AccuAirQualityData>? = null
)
