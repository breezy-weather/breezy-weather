package org.breezyweather.weather.json.accu

import kotlinx.serialization.Serializable

@Serializable
data class AccuAirQualityResult(
    val data: List<AccuAirQualityData>?
)
