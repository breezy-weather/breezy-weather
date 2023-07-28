package org.breezyweather.sources.here.json

import kotlinx.serialization.Serializable

@Serializable
data class HereGeocodingResult(
    val items: List<HereGeocodingData>?
)
