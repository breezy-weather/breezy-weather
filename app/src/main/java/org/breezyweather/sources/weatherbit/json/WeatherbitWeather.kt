package org.breezyweather.sources.weatherbit.json

import kotlinx.serialization.Serializable

@Serializable
data class WeatherbitWeather(
    val code: Int?,
    val description: String?,
    val icon: String?
)
