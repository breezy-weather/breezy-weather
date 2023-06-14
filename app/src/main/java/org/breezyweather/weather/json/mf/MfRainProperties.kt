package org.breezyweather.weather.json.mf

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MfRainProperties(
    @SerialName("forecast") val rainForecasts: List<MfRainForecast>?
)
