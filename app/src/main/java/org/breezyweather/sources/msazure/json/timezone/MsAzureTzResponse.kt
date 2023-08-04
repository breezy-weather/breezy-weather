package org.breezyweather.sources.msazure.json.timezone
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MsAzureTzResponse(
    @SerialName("TimeZones") val timeZones: List<MsAzureTz>?
)