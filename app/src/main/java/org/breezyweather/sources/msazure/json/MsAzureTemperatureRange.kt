package org.breezyweather.sources.msazure.json

import kotlinx.serialization.Serializable

@Serializable
data class MsAzureTemperatureRange(
    val minimum: MsAzureWeatherUnit?,
    val maximum: MsAzureWeatherUnit?
)