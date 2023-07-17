package org.breezyweather.sources.accu.json

import kotlinx.serialization.Serializable

/**
 * Accu daily result.
 */
@Serializable
data class AccuForecastDailyResult(
    val Headline: AccuForecastHeadline?,
    val DailyForecasts: List<AccuForecastDailyForecast>?
)
