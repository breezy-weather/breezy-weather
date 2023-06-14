package org.breezyweather.weather.json.metno

import kotlinx.serialization.Serializable

@Serializable
data class MetNoEphemerisLocation(
    val time: List<MetNoEphemerisTime>?
)
