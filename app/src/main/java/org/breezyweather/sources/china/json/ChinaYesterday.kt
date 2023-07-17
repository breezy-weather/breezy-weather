package org.breezyweather.sources.china.json

import kotlinx.serialization.Serializable

@Serializable
data class ChinaYesterday(
    val aqi: String?,
    val date: String?,
    val tempMax: String?,
    val tempMin: String?
)
