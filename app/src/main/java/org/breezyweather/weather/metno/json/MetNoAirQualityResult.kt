package org.breezyweather.weather.metno.json

import kotlinx.serialization.Serializable

/**
 * MET Norway air quality
 * Norway only
 */
@Serializable
data class MetNoAirQualityResult(
    val data: MetNoAirQualityData? = null
)
