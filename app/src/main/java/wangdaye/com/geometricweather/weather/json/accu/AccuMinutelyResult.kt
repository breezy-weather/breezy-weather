package wangdaye.com.geometricweather.weather.json.accu

import kotlinx.serialization.Serializable

@Serializable
data class AccuMinutelyResult(
    val Summary: AccuMinutelySummary?,
    val Intervals: List<AccuMinutelyInterval>?
)
