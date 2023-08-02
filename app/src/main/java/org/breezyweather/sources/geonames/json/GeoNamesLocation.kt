package org.breezyweather.sources.geonames.json

import kotlinx.serialization.Serializable

@Serializable
data class GeoNamesLocation(
    val lng: Double,
    val lat: Double,
    val timezone: GeoNamesTimeZone?,
    val geonameId: Int?,
    val name: String?,
    val alternateNames: List<GeoNamesAlternateName>?,
    val adminName1: String?,
    val adminCode1: String?,
    val adminName2: String?,
    val adminCode2: String?,
    val adminName3: String?,
    val adminCode3: String?,
    val adminName4: String?,
    val adminCode4: String?,
    val countryName: String?,
    val countryCode: String?
)
