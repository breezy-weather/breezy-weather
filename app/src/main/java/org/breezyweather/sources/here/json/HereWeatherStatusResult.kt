package org.breezyweather.sources.here.json

import kotlinx.serialization.Serializable

@Serializable
data class HereWeatherStatusResult(
    val status: String?
)
