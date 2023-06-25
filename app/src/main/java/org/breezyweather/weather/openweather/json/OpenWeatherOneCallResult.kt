package org.breezyweather.weather.openweather.json

import kotlinx.serialization.Serializable

/**
 * OpenWeather One Call result.
 */
@Serializable
data class OpenWeatherOneCallResult(
    val lat: Float?,
    val lon: Float?,
    val timezone: String?,
    val current: OpenWeatherOneCallCurrent?,
    val minutely: List<OpenWeatherOneCallMinutely>?,
    val hourly: List<OpenWeatherOneCallHourly>?,
    val daily: List<OpenWeatherOneCallDaily>?,
    val alerts: List<OpenWeatherOneCallAlert>?
)