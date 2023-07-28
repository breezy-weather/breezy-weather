package org.breezyweather.sources.here.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HereWeatherLocation(
    @SerialName("lat") val latitude: Float?,
    @SerialName("lng") val longitude: Float?,
    @SerialName("elv") val elevation: Float?
)
