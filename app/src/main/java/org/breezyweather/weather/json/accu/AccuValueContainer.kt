package org.breezyweather.weather.json.accu

import kotlinx.serialization.Serializable

@Serializable
data class AccuValueContainer(
    val Metric: AccuValue?
)
