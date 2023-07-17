package org.breezyweather.sources.mf.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MfWarningMaxCountItems(
    @SerialName("color_id") val colorId: Int,
    val count: Int,
    @SerialName("text_count") val textCount: String?
)
