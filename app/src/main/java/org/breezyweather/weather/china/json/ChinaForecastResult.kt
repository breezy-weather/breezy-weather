package org.breezyweather.weather.china.json

import kotlinx.serialization.Serializable

@Serializable
data class ChinaForecastResult(
    val current: ChinaCurrent?,
    val forecastDaily: ChinaForecastDaily?,
    val forecastHourly: ChinaForecastHourly?,
    val yesterday: ChinaYesterday?,
    val updateTime: Long,
    val aqi: ChinaAqi?,
    val alerts: List<ChinaAlert>?
)
