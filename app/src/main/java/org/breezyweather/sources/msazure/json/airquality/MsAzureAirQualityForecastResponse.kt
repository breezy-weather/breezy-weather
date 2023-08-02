package org.breezyweather.sources.msazure.json.airquality

import kotlinx.serialization.Serializable

@Serializable
data class MsAzureAirQualityForecastResponse(
    val results: List<MsAzureAirQualityForecast>?
)
