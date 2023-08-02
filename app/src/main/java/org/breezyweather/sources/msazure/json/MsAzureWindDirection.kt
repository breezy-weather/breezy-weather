package org.breezyweather.sources.msazure.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MsAzureWindDirection(
    @SerialName("degrees") val degrees: Double?,
    @SerialName("localizedDescription") val localizedDescription: String?
)
