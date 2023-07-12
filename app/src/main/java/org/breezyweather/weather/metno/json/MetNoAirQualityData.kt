package org.breezyweather.weather.metno.json

import kotlinx.serialization.Serializable

@Serializable
data class MetNoAirQualityData(
    val time: List<MetNoAirQualityTime>?
)
