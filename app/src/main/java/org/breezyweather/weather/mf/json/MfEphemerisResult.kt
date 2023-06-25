package org.breezyweather.weather.mf.json

import kotlinx.serialization.Serializable

@Serializable
data class MfEphemerisResult(
    val properties: MfEphemerisProperties?
)