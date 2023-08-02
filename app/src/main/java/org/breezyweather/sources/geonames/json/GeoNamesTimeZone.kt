package org.breezyweather.sources.geonames.json

import kotlinx.serialization.Serializable

@Serializable
data class GeoNamesTimeZone(
    val timeZoneId: String?
)
