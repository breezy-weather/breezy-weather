package org.breezyweather.sources.geonames.json

import kotlinx.serialization.Serializable

@Serializable
data class GeoNamesSearchResult(
    val status: GeoNamesStatus?,
    val geonames: List<GeoNamesLocation>?
)
