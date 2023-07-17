package org.breezyweather.sources.accu.json

import kotlinx.serialization.Serializable

@Serializable
data class AccuLocationGeoPosition(
    val Latitude: Double,
    val Longitude: Double
)
