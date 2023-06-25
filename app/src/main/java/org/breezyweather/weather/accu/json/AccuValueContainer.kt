package org.breezyweather.weather.accu.json

import kotlinx.serialization.Serializable

@Serializable
data class AccuValueContainer(
    val Metric: AccuValue?
)
