package org.breezyweather.weather.accu.json

import kotlinx.serialization.Serializable

@Serializable
data class AccuMinutelyResult(
    val Summary: AccuMinutelySummary? = null,
    val Intervals: List<AccuMinutelyInterval>? = null
)
