package org.breezyweather.weather.json.china

import kotlinx.serialization.Serializable

@Serializable
data class ChinaHourlyWindValue(
    val direction: String?,
    val speed: String?
)
