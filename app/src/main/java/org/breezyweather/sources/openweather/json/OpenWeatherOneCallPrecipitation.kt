package org.breezyweather.sources.openweather.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenWeatherOneCallPrecipitation(
    @SerialName("1h") val cumul1h: Float?
)
