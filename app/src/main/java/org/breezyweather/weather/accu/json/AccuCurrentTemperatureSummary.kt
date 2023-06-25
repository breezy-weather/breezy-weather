package org.breezyweather.weather.accu.json

import kotlinx.serialization.Serializable

@Serializable
data class AccuCurrentTemperatureSummary(
    val Past24HourRange: AccuCurrentTemperaturePast24HourRange?
)
