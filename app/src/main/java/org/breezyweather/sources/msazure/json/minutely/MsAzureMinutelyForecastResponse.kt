package org.breezyweather.sources.msazure.json.minutely

import kotlinx.serialization.Serializable

@Serializable
data class MsAzureMinutelyForecastResponse(
    val intervals: List<MsAzureMinutelyForecast>? = null
)
