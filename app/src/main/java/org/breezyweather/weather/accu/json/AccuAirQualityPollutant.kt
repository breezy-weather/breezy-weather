package org.breezyweather.weather.accu.json

import kotlinx.serialization.Serializable

@Serializable
data class AccuAirQualityPollutant(
    val type: String,
    val concentration: org.breezyweather.weather.accu.json.AccuAirQualityConcentration
)
