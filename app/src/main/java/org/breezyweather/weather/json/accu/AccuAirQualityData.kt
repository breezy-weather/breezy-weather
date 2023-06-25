package org.breezyweather.weather.json.accu

import kotlinx.serialization.Serializable

/**
 * Accu air quality hourly forecast result.
 */
@Serializable
data class AccuAirQualityData(
    val epochDate: Long,
    val pollutants: List<AccuAirQualityPollutant>?,
)
