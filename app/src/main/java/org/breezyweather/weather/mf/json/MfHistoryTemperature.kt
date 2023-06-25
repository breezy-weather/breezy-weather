package org.breezyweather.weather.mf.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MfHistoryTemperature(
    val value: Float?,
    @SerialName("windchill") val windChill: Float?
)
