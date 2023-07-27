package org.breezyweather.sources.pirateweather.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PirateWeatherAlert (
    val title: String?,
    @SerialName("time") val start: Long,
    @SerialName("expires") val end: Long,
    val description: String?,
    val regions: List<String>?,
    val severity: String?,
    val uri: String?
)