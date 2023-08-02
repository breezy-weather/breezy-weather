package org.breezyweather.sources.msazure.json.airquality

import kotlinx.serialization.Serializable
import org.breezyweather.sources.msazure.json.MsAzureWeatherUnit

@Serializable
data class MsAzureAirPollutant(
    val type: String?,
    val concentration: MsAzureWeatherUnit?
)