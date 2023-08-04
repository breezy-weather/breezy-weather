package org.breezyweather.sources.msazure.json.geocoding
import kotlinx.serialization.Serializable

@Serializable
data class MsAzureGeocodingResponse(
    val features: List<MsAzureGeocoding>?
)