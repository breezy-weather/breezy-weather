package org.breezyweather.sources.msazure.json.current

import kotlinx.serialization.Serializable

@Serializable
data class MsAzureCurrentConditionsResponse(
    val results: List<MsAzureCurrentConditions>?
)

