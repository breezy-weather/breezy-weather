package org.breezyweather.sources.metno.json

import kotlinx.serialization.Serializable

@Serializable
data class MetNoMoonProperties(
    val moonrise: MetNoEphemerisProperty?,
    val moonset: MetNoEphemerisProperty?,
    val moonphase: Float?,
)
