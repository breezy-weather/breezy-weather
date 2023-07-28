package org.breezyweather.sources.pirateweather.json

import kotlinx.serialization.Serializable

@Serializable
data class PirateWeatherForecast<T>(
    val summary: String?,
    val icon: String?,
    val data: List<T>?
)
