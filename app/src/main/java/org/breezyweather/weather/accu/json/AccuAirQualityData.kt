package org.breezyweather.weather.accu.json

import kotlinx.serialization.Serializable

/**
 * Accu air quality hourly forecast result.
 */
@Serializable
data class AccuAirQualityData(
    val epochDate: Long,
    val pollutants: List<AccuAirQualityPollutant>?,
)
