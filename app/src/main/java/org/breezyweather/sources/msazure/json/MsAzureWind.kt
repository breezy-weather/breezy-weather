package org.breezyweather.sources.msazure.json

import kotlinx.serialization.Serializable

@Serializable
data class MsAzureWind(
    val direction: MsAzureWindDirection?,
    val speed: MsAzureWeatherUnit?
)