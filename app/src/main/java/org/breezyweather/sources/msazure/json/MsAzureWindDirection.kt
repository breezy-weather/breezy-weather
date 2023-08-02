package org.breezyweather.sources.msazure.json

import kotlinx.serialization.Serializable

@Serializable
data class MsAzureWindDirection(
    val degrees: Double?,
    val localizedDescription: String?
)
