package wangdaye.com.geometricweather.weather.json.mf

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MfWarningConsequence(
    @SerialName("phenomenon_max_color_id") val phenomenoMaxColorId: Int,
    @SerialName("phenomenon_id") val phenomenonId: Int,
    @SerialName("text_consequence") val textConsequence: String?
)
