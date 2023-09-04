package org.breezyweather.remoteviews.gadgetbridge.json

import kotlinx.serialization.Serializable

@Serializable
data class GadgetBridgeHourlyForecast(
    val timestamp: Int? = null,
    val temp: Int? = null,
    val conditionCode: Int? = null,
    val humidity: Int? = null,
    val windSpeed: Float? = null,
    val windDirection: Int? = null,
    val uvIndex: Float? = null,
    val precipProbability: Int? = null,
)
