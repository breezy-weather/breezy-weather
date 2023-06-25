package org.breezyweather.weather.china.json

import kotlinx.serialization.Serializable

@Serializable
data class ChinaDailyWind(
    val direction: org.breezyweather.weather.china.json.ChinaValueListChinaFromTo?,
    val speed: org.breezyweather.weather.china.json.ChinaValueListChinaFromTo?
)
