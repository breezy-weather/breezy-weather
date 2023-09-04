package org.breezyweather.remoteviews.gadgetbridge.json

import kotlinx.serialization.Serializable

@Serializable
data class GadgetBridgeAirQuality(
    val aqi: Int? = null,
    val co: Float? = null,
    val no2: Float? = null,
    val o3: Float? = null,
    val pm10: Float? = null,
    val pm25: Float? = null,
    val so2: Float? = null,
    val coAqi: Int? = null,
    val no2Aqi: Int? = null,
    val o3Aqi: Int? = null,
    val pm10Aqi: Int? = null,
    val pm25Aqi: Int? = null,
    val so2Aqi: Int? = null,
)
