package org.breezyweather.sources.qweather.json

import kotlinx.serialization.Serializable

@Serializable
data class QWeatherCurrentAQIResult(
    val code: String,
    val now: QWeatherAQIProperties
)