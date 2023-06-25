package org.breezyweather.weather.accu.json

import kotlinx.serialization.Serializable

@Serializable
data class AccuForecastMoon(
    val EpochRise: Long?,
    val EpochSet: Long?,
    val Phase: String?
)
