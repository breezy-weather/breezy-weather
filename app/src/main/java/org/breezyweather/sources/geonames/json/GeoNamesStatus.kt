package org.breezyweather.sources.geonames.json

import kotlinx.serialization.Serializable

@Serializable
data class GeoNamesStatus(
    val message: String?,
    val value: Int?
)
