package org.breezyweather.weather.china.json

import kotlinx.serialization.Serializable

@Serializable
data class ChinaMinutelyResult(
    val precipitation: ChinaMinutelyPrecipitation?
)
