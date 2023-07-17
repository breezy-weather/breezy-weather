package org.breezyweather.sources.metno.json

import kotlinx.serialization.Serializable

/**
 * MET Norway air quality
 * Norway only
 */
@Serializable
data class MetNoAirQualityResult(
    val data: MetNoAirQualityData? = null
)
