package org.breezyweather.sources.geonames.json

import kotlinx.serialization.Serializable

@Serializable
data class GeoNamesAlternateName(
    val name: String?,
    val lang: String?
)
