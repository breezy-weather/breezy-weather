package org.breezyweather.weather.china.json

import kotlinx.serialization.Serializable

@Serializable
data class ChinaForecastDaily(
    val precipitationProbability: org.breezyweather.weather.china.json.ChinaPrecipitationProbability?,
    val pubTime: String?,
    val sunRiseSet: org.breezyweather.weather.china.json.ChinaSunRiseSet?,
    val temperature: org.breezyweather.weather.china.json.ChinaValueListChinaFromTo?,
    val weather: org.breezyweather.weather.china.json.ChinaValueListChinaFromTo?,
    val wind: org.breezyweather.weather.china.json.ChinaDailyWind?
)
