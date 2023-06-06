package wangdaye.com.geometricweather.weather.json.mf

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import wangdaye.com.geometricweather.common.serializer.DateSerializer
import java.util.Date

@Serializable
data class MfRainForecast(
    @Serializable(DateSerializer::class) val time: Date,
    @SerialName("rain_intensity") val rainIntensity: Int?,
    @SerialName("rain_intensity_description") val rainIntensityDescription: String?
)
