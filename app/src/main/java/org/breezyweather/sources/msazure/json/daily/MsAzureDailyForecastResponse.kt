package org.breezyweather.sources.msazure.json.daily

import kotlinx.serialization.Serializable


@Serializable
data class MsAzureDailyForecastResponse(
    val forecasts: List<MsAzureDailyForecast>? = null
)