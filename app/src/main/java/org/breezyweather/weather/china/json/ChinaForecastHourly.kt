package org.breezyweather.weather.china.json

import kotlinx.serialization.Serializable

@Serializable
data class ChinaForecastHourly(
    val desc: String?,
    val temperature: org.breezyweather.weather.china.json.ChinaValueListInt?,
    val weather: org.breezyweather.weather.china.json.ChinaValueListInt?,
    val wind: org.breezyweather.weather.china.json.ChinaHourlyWind?
)
