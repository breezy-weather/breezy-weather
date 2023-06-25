package org.breezyweather.weather.accu.json

import kotlinx.serialization.Serializable

@Serializable
data class AccuForecastAirAndPollen(
    val Name: String?,
    val Value: Int?,
    val Category: String?,
    val CategoryValue: Int?,
    val Type: String?
)
