package org.breezyweather.sources.here.json

import kotlinx.serialization.Serializable

@Serializable
data class HereWeatherNWSAlerts(
    val warnings: List<HereNWSAlertItem>?,
    val watches: List<HereNWSAlertItem>?
)
