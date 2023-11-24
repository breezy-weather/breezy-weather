package org.breezyweather.sources.qweather.json

import kotlinx.serialization.Serializable

@Serializable
data class QWeatherLocationProperties(
    val name: String,
    val id: String,
    val lat: Float,
    val lon: Float,
    val adm1: String,
    val adm2: String,
    val country: String,
    val tz: String,
    val utcOffset: String,
    val isDst: String,
    val type: String,
)
