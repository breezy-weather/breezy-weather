package org.breezyweather.sources.msazure.json.alerts

import kotlinx.serialization.Serializable

@Serializable
data class MsAzureWeatherAlert(
    val alertId: Long,
    val description: MsAzureWeatherAlertDescription?,
    val category: String?,
    val priority: Int?,
    val alertAreas: List<MsAzureWeatherAlertArea>?
)