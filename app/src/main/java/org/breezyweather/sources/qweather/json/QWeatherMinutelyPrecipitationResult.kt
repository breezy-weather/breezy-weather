package org.breezyweather.sources.qweather.json

import kotlinx.serialization.Serializable

@Serializable
data class QWeatherMinutelyPrecipitationResult(
    val code: String,
    val summary: String,
    val minutely: List<QWeatherMinuelyPrecipitationProperties>,
)