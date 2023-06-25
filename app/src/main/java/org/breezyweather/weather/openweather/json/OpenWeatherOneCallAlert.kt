package org.breezyweather.weather.openweather.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenWeatherOneCallAlert(
    @SerialName("sender_name") val senderName: String?,
    val event: String?,
    val start: Long,
    val end: Long,
    val description: String?
)
