package org.breezyweather.weather.accu.json

import kotlinx.serialization.Serializable

@Serializable
data class AccuCurrentPrecipitationSummary(
    val Precipitation: AccuValueContainer?
)
