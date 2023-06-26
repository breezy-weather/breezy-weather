package org.breezyweather.weather.china.json

import kotlinx.serialization.Serializable

@Serializable
data class ChinaForecastHourly(
    val desc: String?,
    val temperature: ChinaValueListInt?,
    val weather: ChinaValueListInt?,
    val wind: ChinaHourlyWind?
)
