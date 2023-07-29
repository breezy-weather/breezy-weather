package org.breezyweather.sources.weatherbit.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherbitAirQuality(
    val aqi: Int?,
    val so2: Int?,
    val no2: Int?,
    val o3: Int?,
    val pm25: Int?,
    val pm10: Int?
)