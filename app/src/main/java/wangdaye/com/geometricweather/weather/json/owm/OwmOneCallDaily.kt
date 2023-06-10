package wangdaye.com.geometricweather.weather.json.owm

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OwmOneCallDaily(
    val dt: Long,
    val sunrise: Long?,
    val sunset: Long?,
    val moonrise: Long?,
    val moonset: Long?,
    val temp: OwmOneCallDailyTemp?,
    @SerialName("feels_like") val feelsLike: OwmOneCallDailyFeelsLike?,
    val pressure: Int?,
    val humidity: Int?,
    @SerialName("dew_point") val dewPoint: Float?,
    @SerialName("wind_speed") val windSpeed: Float?,
    @SerialName("wind_deg") val windDeg: Int?,
    val weather: List<OwmOneCallWeather>?,
    val clouds: Int?,
    val pop: Float?,
    val rain: Float?,
    val snow: Float?,
    val uvi: Float?
)
