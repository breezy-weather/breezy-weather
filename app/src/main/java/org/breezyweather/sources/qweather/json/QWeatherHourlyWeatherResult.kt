package org.breezyweather.sources.qweather.json

import kotlinx.serialization.Serializable

@Serializable
data class QWeatherHourlyWeatherResult(
    val code: String,
    val hourly: List<QWeatherInstantWeatherProperties>,
)