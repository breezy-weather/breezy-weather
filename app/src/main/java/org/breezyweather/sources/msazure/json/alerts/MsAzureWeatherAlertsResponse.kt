package org.breezyweather.sources.msazure.json.alerts

import kotlinx.serialization.Serializable


@Serializable
data class MsAzureWeatherAlertsResponse(
    val results: List<MsAzureWeatherAlert>?
)


