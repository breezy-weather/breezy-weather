package org.breezyweather.sources.accu.json

import kotlinx.serialization.Serializable

@Serializable
data class AccuAirQualityConcentration(
    val value: Double?
)
