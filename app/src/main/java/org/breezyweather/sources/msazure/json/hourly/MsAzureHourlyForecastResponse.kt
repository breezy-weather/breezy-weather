package org.breezyweather.sources.msazure.json.hourly

import kotlinx.serialization.Serializable

@Serializable
data class MsAzureHourlyForecastResponse(
    val forecasts: List<MsAzureHourlyForecast>?
)