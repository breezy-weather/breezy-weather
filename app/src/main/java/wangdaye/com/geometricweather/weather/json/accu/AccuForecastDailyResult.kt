package wangdaye.com.geometricweather.weather.json.accu

import kotlinx.serialization.Serializable

/**
 * Accu daily result.
 */
@Serializable
data class AccuForecastDailyResult(
    val Headline: AccuForecastHeadline?,
    val DailyForecasts: List<AccuForecastDailyForecast>?
)
