package org.breezyweather.weather.metno.json

import kotlinx.serialization.Serializable

@Serializable
data class MetNoEphemerisTime(
    val date: String?,
    val moonposition: MetNoEphemerisMoonPosition?,
    val moonrise: MetNoEphemerisPhase?,
    val moonset: MetNoEphemerisPhase?,
    val sunrise: MetNoEphemerisPhase?,
    val sunset: MetNoEphemerisPhase?
)
