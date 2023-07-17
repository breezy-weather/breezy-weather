package org.breezyweather.sources.accu.json

import kotlinx.serialization.Serializable

@Serializable
data class AccuCurrentPrecipitationSummary(
    val Precipitation: AccuValueContainer?
)
