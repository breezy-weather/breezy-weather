package org.breezyweather.sources.pirateweather.json

import kotlinx.serialization.Serializable

@Serializable
data class PirateWeatherFlags(
    val sources: List<String>,
    val sourceTimes: PirateWeatherFlagsSourceTimes?,
    val nearestStation: Int?,
    val units: String?,
    val version: String?
)
