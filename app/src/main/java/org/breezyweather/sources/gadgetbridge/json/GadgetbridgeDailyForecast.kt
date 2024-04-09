package org.breezyweather.sources.gadgetbridge.json

import kotlinx.serialization.Serializable

@Serializable
data class GadgetbridgeDailyForecast(
    val minTemp: Int? = null,
    val maxTemp: Int? = null,
    val conditionCode: Int? = null,
    val humidity: Int? = null,
    val windSpeed: Float? = null,
    val windDirection: Int? = null,
    val uvIndex: Float? = null,
    val precipProbability: Int? = null,
    val sunRise: Int? = null,
    val sunSet: Int? = null,
    val moonRise: Int? = null,
    val moonSet: Int? = null,
    val moonPhase: Int? = null,
    val airQuality: GadgetbridgeAirQuality? = null,
)
