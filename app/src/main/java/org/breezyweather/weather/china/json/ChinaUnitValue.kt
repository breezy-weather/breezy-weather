package org.breezyweather.weather.china.json

import kotlinx.serialization.Serializable

@Serializable
data class ChinaUnitValue(
    val unit: String?,
    val value: String?
)
