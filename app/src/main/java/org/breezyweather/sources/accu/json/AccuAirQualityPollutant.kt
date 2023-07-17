package org.breezyweather.sources.accu.json

import kotlinx.serialization.Serializable

@Serializable
data class AccuAirQualityPollutant(
    val type: String,
    val concentration: AccuAirQualityConcentration
)
