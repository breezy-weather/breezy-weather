package org.breezyweather.sources.veduris.json

import kotlinx.serialization.Serializable

@Serializable
data class VedurIsDailyForecast(
    val forecastDate: String,
    val hourlyForecasts: List<VedurIsHourlyForecast>?,
)
