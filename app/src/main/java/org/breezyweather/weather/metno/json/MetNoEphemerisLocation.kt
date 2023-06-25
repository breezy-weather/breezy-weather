package org.breezyweather.weather.metno.json

import kotlinx.serialization.Serializable

@Serializable
data class MetNoEphemerisLocation(
    val time: List<MetNoEphemerisTime>?
)
