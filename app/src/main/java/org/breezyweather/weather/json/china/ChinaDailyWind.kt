package org.breezyweather.weather.json.china

import kotlinx.serialization.Serializable

@Serializable
data class ChinaDailyWind(
    val direction: ChinaValueListChinaFromTo?,
    val speed: ChinaValueListChinaFromTo?
)
