package org.breezyweather.sources.msazure.json.timezone

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MsAzureTz(
    @SerialName("Id") val id: String?,
    @SerialName("ReferenceTime") val referenceTime: MsAzureTzSunrise?
)