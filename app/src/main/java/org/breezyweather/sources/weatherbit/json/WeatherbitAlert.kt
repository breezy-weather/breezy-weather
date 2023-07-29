package org.breezyweather.sources.weatherbit.json

import kotlinx.serialization.Serializable

import kotlinx.serialization.SerialName
import org.breezyweather.common.serializer.DateSerializer
import java.util.Date


@Serializable
data class WeatherbitAlert(
    @SerialName("title") val title: String?,
    @SerialName("description") val description: String?,
    @SerialName("effective_utc") val effectiveUtc: String,
    @SerialName("expires_utc") val expiresUtc: String,
    @SerialName("onset_utc") val onsetUtc: String?,
    @SerialName("ends_utc") val endsUtc: String?,
    @SerialName("severity") val severity: String?
)