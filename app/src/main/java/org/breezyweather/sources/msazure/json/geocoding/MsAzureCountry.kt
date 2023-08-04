package org.breezyweather.sources.msazure.json.geocoding

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MsAzureCountry(
    val name: String?,
    @SerialName("ISO") val code: String?
)