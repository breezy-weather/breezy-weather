package org.breezyweather.sources.openweather.json

import kotlinx.serialization.Serializable

/**
 * OpenWeather One Call result.
 */
@Serializable
data class OpenWeatherOneCallResult(
    val lat: Float? = null,
    val lon: Float? = null,
    val timezone: String? = null,
    val current: OpenWeatherOneCallCurrent? = null,
    val minutely: List<OpenWeatherOneCallMinutely>? = null,
    val hourly: List<OpenWeatherOneCallHourly>? = null,
    val daily: List<OpenWeatherOneCallDaily>? = null,
    val alerts: List<OpenWeatherOneCallAlert>? = null
)