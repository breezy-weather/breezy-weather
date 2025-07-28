package org.breezyweather.sources.veduris.json

import kotlinx.serialization.Serializable

@Serializable
data class VedurIsHourlyForecast(
    val forecastTime: String,
    val forecastDate: String,
    val icon: String?,
    val temperature: Double?,
    val windSpeed: Double?,
    val windDirection: Double?,
    val precipitation: Double?,
    val humidity: Double?,
)
