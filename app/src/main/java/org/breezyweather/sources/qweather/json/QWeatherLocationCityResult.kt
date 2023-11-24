package org.breezyweather.sources.qweather.json

import kotlinx.serialization.Serializable

@Serializable
data class QWeatherLocationCityResult(
    val code: String,
    val location: List<QWeatherLocationProperties>?,
)