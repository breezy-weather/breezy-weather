package org.breezyweather.sources.qweather.json

import kotlinx.serialization.Serializable

@Serializable
data class QWeatherAQIProperties(
    val aqi: String,
    val level: String,
    val category: String,
    val primary: String,
    val pm10: String,
    val pm2p5: String,
    val no2: String,
    val so2: String,
    val co: String,
    val o3: String,
)
