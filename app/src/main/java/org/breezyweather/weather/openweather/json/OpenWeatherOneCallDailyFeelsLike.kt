package org.breezyweather.weather.openweather.json

import kotlinx.serialization.Serializable

@Serializable
data class OpenWeatherOneCallDailyFeelsLike(
    val day: Float?,
    val night: Float?,
    val eve: Float?,
    val morn: Float?
)