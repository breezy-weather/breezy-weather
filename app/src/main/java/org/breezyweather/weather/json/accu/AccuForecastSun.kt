package org.breezyweather.weather.json.accu

import kotlinx.serialization.Serializable

@Serializable
data class AccuForecastSun(
    val EpochRise: Long,
    val EpochSet: Long
)
