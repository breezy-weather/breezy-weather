package org.breezyweather.weather.metno.json

import kotlinx.serialization.Serializable

/**
 * MET Norway sun/moon rise/set forecast.
 */
@Serializable
data class MetNoSunResult(
    val properties: MetNoSunProperties?
)
