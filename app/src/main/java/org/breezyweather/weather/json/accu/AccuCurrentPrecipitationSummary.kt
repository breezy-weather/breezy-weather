package org.breezyweather.weather.json.accu

import kotlinx.serialization.Serializable

@Serializable
data class AccuCurrentPrecipitationSummary(
    val Precipitation: AccuValueContainer?
)
