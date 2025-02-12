package org.breezyweather.sources.nominatim.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NominatimLocationResult(
    @SerialName("place_id") val placeId: Int?,
    val name: String,
    val address: NominatimAddress? = null,
)
