package org.breezyweather.weather.mf.json

import kotlinx.serialization.Serializable

@Serializable
data class MfEphemerisProperties(
    val ephemeris: MfEphemeris?
)
