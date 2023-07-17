package org.breezyweather.sources.accu.json

import kotlinx.serialization.Serializable

@Serializable
data class AccuCurrentTemperatureSummary(
    val Past24HourRange: AccuCurrentTemperaturePast24HourRange?
)
