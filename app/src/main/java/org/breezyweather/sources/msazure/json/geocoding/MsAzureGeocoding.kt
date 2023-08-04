package org.breezyweather.sources.msazure.json.geocoding

import kotlinx.serialization.Serializable

@Serializable
data class MsAzureGeocoding(
    val properties: MsAzureGeocodingProperties?,
    val geometry: MsAzureGeocodingGeometry?
)