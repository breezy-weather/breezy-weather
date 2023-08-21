package org.breezyweather.remoteviews.gadgetbridge.json

import kotlinx.serialization.Serializable

@Serializable
data class GadgetBridgeDailyForecast(
    val minTemp: Int? = null,
    val maxTemp: Int? = null,
    val conditionCode: Int? = null,
    val humidity: Int? = null
)
