package org.breezyweather.weather.mf.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import java.util.Date

@Serializable
data class MfForecastHourly(
    @Serializable(DateSerializer::class) val time: Date,
    @SerialName("T") val t: Float?,
    @SerialName("T_windchill") val tWindchill: Float?,
    @SerialName("weather_icon") val weatherIcon: String?,
    @SerialName("weather_description") val weatherDescription: String?,
    @SerialName("wind_direction") val windDirection: Int?,
    @SerialName("wind_icon") val windIcon: String?,
    @SerialName("wind_speed") val windSpeed: Int?,
    @SerialName("wind_speed_gust") val windSpeedGust: Int?,
    @SerialName("rain_1h") val rain1h: Float?,
    @SerialName("rain_3h") val rain3h: Float?,
    @SerialName("rain_6h") val rain6h: Float?,
    @SerialName("rain_12h") val rain12h: Float?,
    @SerialName("rain_24h") val rain24h: Float?,
    @SerialName("relative_humidity") val relativeHumidity: Int?,
    @SerialName("snow_1h") val snow1h: Float?,
    @SerialName("snow_3h") val snow3h: Float?,
    @SerialName("snow_6h") val snow6h: Float?,
    @SerialName("snow_12h") val snow12h: Float?,
    @SerialName("snow_24h") val snow24h: Float?,
    @SerialName("total_cloud_cover") val totalCloudCover: Int?
)
