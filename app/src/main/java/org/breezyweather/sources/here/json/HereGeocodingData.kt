package org.breezyweather.sources.here.json

import kotlinx.serialization.Serializable

@Serializable
data class HereGeocodingData(
    val id: String,
    val position: HereGeocodingPosition,
    val address: HereGeocodingAddress,
    val timeZone: HereGeocodingTimezone
)
