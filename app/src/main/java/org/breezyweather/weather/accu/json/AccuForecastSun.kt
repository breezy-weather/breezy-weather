package org.breezyweather.weather.accu.json

import kotlinx.serialization.Serializable

@Serializable
data class AccuForecastSun(
    val EpochRise: Long,
    val EpochSet: Long
)
