package org.breezyweather.sources.accu.json

import kotlinx.serialization.Serializable

@Serializable
data class AccuForecastSun(
    val EpochRise: Long?,
    val EpochSet: Long?
)
