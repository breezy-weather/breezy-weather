package org.breezyweather.weather.json.accu

import kotlinx.serialization.Serializable

@Serializable
data class AccuCurrentTemperatureSummary(
    val Past24HourRange: AccuCurrentTemperaturePast24HourRange?
)
