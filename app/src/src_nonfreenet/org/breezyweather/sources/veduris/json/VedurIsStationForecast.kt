package org.breezyweather.sources.veduris.json

import kotlinx.serialization.Serializable

@Serializable
data class VedurIsStationForecast(
    val hourlyForecasts: List<VedurIsHourlyForecast>?,
    val dailyForecasts: List<VedurIsDailyForecast>?,
)
