package org.breezyweather.sources.mf.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MfWarningConsequence(
    @SerialName("phenomenon_max_color_id") val phenomenoMaxColorId: Int,
    @SerialName("phenomenon_id") val phenomenonId: String,
    @SerialName("text_consequence") val textConsequence: String?
)
