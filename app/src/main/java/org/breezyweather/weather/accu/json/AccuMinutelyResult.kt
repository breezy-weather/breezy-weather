package org.breezyweather.weather.accu.json

import kotlinx.serialization.Serializable

@Serializable
data class AccuMinutelyResult(
    val Summary: AccuMinutelySummary?,
    val Intervals: List<AccuMinutelyInterval>?
)
