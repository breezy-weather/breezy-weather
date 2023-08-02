package org.breezyweather.sources.msazure.json

import kotlinx.serialization.Serializable

@Serializable
data class MsAzureWeatherUnit(
    val value: Double?
)