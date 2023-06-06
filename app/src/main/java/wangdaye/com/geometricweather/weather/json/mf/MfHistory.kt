package wangdaye.com.geometricweather.weather.json.mf

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import wangdaye.com.geometricweather.common.serializer.DateSerializer
import java.util.Date

@Serializable
data class MfHistory(
    @Serializable(DateSerializer::class) val dt: Date?,
    @SerialName("T") val temperature: MfHistoryTemperature?
)
