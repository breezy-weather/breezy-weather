package org.breezyweather.weather.json.china

import kotlinx.serialization.Serializable

@Serializable
data class ChinaForecastDaily(
    val precipitationProbability: ChinaPrecipitationProbability?,
    val pubTime: String?,
    val sunRiseSet: ChinaSunRiseSet?,
    val temperature: ChinaValueListChinaFromTo?,
    val weather: ChinaValueListChinaFromTo?,
    val wind: ChinaDailyWind?
)
