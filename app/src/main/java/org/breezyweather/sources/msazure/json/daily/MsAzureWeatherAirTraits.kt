package org.breezyweather.sources.msazure.json.daily

import kotlinx.serialization.Serializable

@Serializable
data class MsAzureWeatherAirTraits(
    val name: String?,
    val value: Int?
)