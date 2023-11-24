package org.breezyweather.sources.qweather.json

import kotlinx.serialization.Serializable

@Serializable
data class QWeatherDailyWeatherResult(
    val code: String,
    val daily: List<QWeatherDayWeatherProperties>,
)