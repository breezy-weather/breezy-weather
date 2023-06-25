package org.breezyweather.weather.metno.json

import kotlinx.serialization.Serializable

/**
 * MET Norway sun/moon rise/set forecast.
 */
@Serializable
data class MetNoEphemerisResult(
    val location: MetNoEphemerisLocation?
)
