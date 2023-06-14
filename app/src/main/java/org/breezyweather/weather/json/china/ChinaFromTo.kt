package org.breezyweather.weather.json.china

import kotlinx.serialization.Serializable

@Serializable
data class ChinaFromTo(
    val from: String?,
    val to: String?
)
