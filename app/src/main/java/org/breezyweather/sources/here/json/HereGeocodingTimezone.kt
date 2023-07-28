package org.breezyweather.sources.here.json

import kotlinx.serialization.Serializable

@Serializable
data class HereGeocodingTimezone(
    val name: String,
    val utcOffset: String
)
