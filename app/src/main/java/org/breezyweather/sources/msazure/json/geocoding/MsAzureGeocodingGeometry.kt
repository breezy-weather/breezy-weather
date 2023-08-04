package org.breezyweather.sources.msazure.json.geocoding

import kotlinx.serialization.Serializable

@Serializable
data class MsAzureGeocodingGeometry(
    val type: String?,
    val coordinates: List<Double?>?
)