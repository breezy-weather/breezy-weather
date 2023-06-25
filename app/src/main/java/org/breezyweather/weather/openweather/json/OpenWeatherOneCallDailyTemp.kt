package org.breezyweather.weather.openweather.json

import kotlinx.serialization.Serializable

@Serializable
data class OpenWeatherOneCallDailyTemp(
    val day: Float?,
    val min: Float?,
    val max: Float?,
    val night: Float?,
    val eve: Float?,
    val morn: Float?
)