package org.breezyweather.sources.qweather.json

import kotlinx.serialization.Serializable

@Serializable
data class QWeatherAlertsResult(
    val code: String,
    val warning: List<QWeatherAlertProperties>?,
)