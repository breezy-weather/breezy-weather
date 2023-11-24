package org.breezyweather.sources.qweather.json

import kotlinx.serialization.Serializable

@Serializable
data class QWeatherLocationPOIResult(
    val code: String,
    val poi: List<QWeatherLocationProperties>?,
)