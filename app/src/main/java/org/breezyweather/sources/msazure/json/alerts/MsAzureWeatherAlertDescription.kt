package org.breezyweather.sources.msazure.json.alerts

import kotlinx.serialization.Serializable

@Serializable
data class MsAzureWeatherAlertDescription(
    val localized: String?,
    val english: String?
)