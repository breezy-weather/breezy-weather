package org.breezyweather.sources.mf.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import java.util.Date

@Serializable
data class MfForecastDaily(
    @Serializable(DateSerializer::class) val time: Date,
    @SerialName("T_min") val tMin: Float?,
    @SerialName("T_max") val tMax: Float?,
    @SerialName("daily_weather_icon") val dailyWeatherIcon: String?,
    @SerialName("daily_weather_description") val dailyWeatherDescription: String?,
    @SerialName("sunrise_time") @Serializable(DateSerializer::class) val sunriseTime: Date?,
    @SerialName("sunset_time") @Serializable(DateSerializer::class) val sunsetTime: Date?,
    @SerialName("uv_index") val uvIndex: Int?
)
