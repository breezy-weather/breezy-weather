package org.breezyweather.sources.msazure.json.geocoding

import kotlinx.serialization.Serializable

@Serializable
data class MsAzureGeocodingProperties(
    val address: MsAzureAddress?,
    val type: String?,
    val confidence: String?
)