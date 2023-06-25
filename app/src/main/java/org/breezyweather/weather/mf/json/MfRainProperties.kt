package org.breezyweather.weather.mf.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MfRainProperties(
    @SerialName("forecast") val rainForecasts: List<MfRainForecast>?
)
