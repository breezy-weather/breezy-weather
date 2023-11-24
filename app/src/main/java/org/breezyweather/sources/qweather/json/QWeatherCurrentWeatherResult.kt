package org.breezyweather.sources.qweather.json

import kotlinx.serialization.Serializable

@Serializable
data class QWeatherCurrentWeatherResult(
    val code: String,
    val now: QWeatherInstantWeatherProperties
)