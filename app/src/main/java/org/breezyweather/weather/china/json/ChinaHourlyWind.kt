package org.breezyweather.weather.china.json

import kotlinx.serialization.Serializable

@Serializable
data class ChinaHourlyWind(
    val value: List<ChinaHourlyWindValue>?
)
