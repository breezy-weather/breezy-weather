package org.breezyweather.sources.here.json

import kotlinx.serialization.Serializable

@Serializable
data class HereWeatherAlert(
    val timeSegments: List<HereWeatherAlertTimeSegment>?,
    val type: Int?,
    val description: String?
)
