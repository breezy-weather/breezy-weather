package org.breezyweather.sources.msazure.json.daily

import kotlinx.serialization.Serializable

@Serializable
data class MsAzureWeatherAirAndPollen(
    val name: String?,
    val value: Int?
)