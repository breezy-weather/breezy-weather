package org.breezyweather.sources.msazure.json.daily

import kotlinx.serialization.Serializable
import org.breezyweather.sources.msazure.json.MsAzureWeatherUnit

@Serializable
data class MsAzureDegreeDay(
    val heating: MsAzureWeatherUnit?,
    val cooling: MsAzureWeatherUnit?
)