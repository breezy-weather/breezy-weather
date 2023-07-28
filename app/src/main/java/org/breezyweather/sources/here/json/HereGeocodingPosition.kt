package org.breezyweather.sources.here.json

import kotlinx.serialization.Serializable

@Serializable
data class HereGeocodingPosition(
    val lat: Float,
    val lng: Float
)
