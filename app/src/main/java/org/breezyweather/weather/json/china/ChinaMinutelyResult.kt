package org.breezyweather.weather.json.china

import kotlinx.serialization.Serializable

@Serializable
data class ChinaMinutelyResult(
    val precipitation: ChinaMinutelyPrecipitation?
)
