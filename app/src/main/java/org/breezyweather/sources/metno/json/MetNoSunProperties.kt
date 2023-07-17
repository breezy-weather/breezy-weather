package org.breezyweather.sources.metno.json

import kotlinx.serialization.Serializable

@Serializable
data class MetNoSunProperties(
    val sunrise: MetNoEphemerisProperty?,
    val sunset: MetNoEphemerisProperty?
)
