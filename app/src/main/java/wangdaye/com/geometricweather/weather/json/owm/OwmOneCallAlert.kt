package wangdaye.com.geometricweather.weather.json.owm

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OwmOneCallAlert(
    @SerialName("sender_name") val senderName: String?,
    val event: String?,
    val start: Long,
    val end: Long,
    val description: String?
)
