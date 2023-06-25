package org.breezyweather.weather.accu.json

import kotlinx.serialization.Serializable

@Serializable
data class AccuCurrentTemperaturePast24HourRange(
    val Minimum: AccuValueContainer?,
    val Maximum: AccuValueContainer?
)
