package org.breezyweather.weather.accu.json

import kotlinx.serialization.Serializable

@Serializable
data class AccuLocationGeoPosition(
    val Latitude: Double,
    val Longitude: Double
)
