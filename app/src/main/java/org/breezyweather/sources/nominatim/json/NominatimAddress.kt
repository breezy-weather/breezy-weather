package org.breezyweather.sources.nominatim.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NominatimAddress(
    val village: String?, // District
    val town: String?, // City
    val municipality: String?, // Admin 3
    val county: String?, // Admin 2
    val state: String?, // Admin 1
    val country: String?,
    @SerialName("country_code") val countryCode: String?,
)
