package org.breezyweather.sources.china.json

import kotlinx.serialization.Serializable

@Serializable
data class ChinaHourlyWindValue(
    val direction: String?,
    val speed: String?
)
