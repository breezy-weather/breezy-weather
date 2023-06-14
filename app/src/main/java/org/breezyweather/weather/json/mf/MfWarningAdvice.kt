package org.breezyweather.weather.json.mf

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MfWarningAdvice(
    @SerialName("phenomenon_max_color_id") val phenomenoMaxColorId: Int,
    @SerialName("phenomenon_id") val phenomenonId: String,
    @SerialName("text_advice") val textAdvice: String?
)
