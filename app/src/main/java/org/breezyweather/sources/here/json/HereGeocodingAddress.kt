package org.breezyweather.sources.here.json

import kotlinx.serialization.Serializable

@Serializable
data class HereGeocodingAddress(
    val label: String,
    val countryCode: String,
    val countryName: String,
    val stateCode: String?,
    val state: String?,
    val county: String?,
    val city: String,
    val postalCode: String
)
