package org.breezyweather.sources.here.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HereWeatherAlertTimeSegment(
    @SerialName("segment") val timeOfDay: String?,
    @SerialName("weekday") val dayOfWeek: String?
)
