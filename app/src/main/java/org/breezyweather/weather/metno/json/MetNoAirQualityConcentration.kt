package org.breezyweather.weather.metno.json

import kotlinx.serialization.Serializable

@Serializable
data class MetNoAirQualityConcentration(
    val value: Float?
)
