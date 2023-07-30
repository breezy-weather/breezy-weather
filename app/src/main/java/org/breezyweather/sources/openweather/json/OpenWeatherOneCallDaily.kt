package org.breezyweather.sources.openweather.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenWeatherOneCallDaily(
    val dt: Long,
    val sunrise: Long?,
    val sunset: Long?,
    val moonrise: Long?,
    val moonset: Long?,
    val temp: OpenWeatherOneCallDailyTemp?,
    @SerialName("feels_like") val feelsLike: OpenWeatherOneCallDailyFeelsLike?,
    val pressure: Int?,
    val humidity: Int?,
    @SerialName("dew_point") val dewPoint: Float?,
    @SerialName("wind_speed") val windSpeed: Float?,
    @SerialName("wind_deg") val windDeg: Int?,
    @SerialName("wind_gust") val windGust: Float?,
    val weather: List<OpenWeatherOneCallWeather>?,
    val clouds: Int?,
    val pop: Float?,
    val rain: Float?,
    val snow: Float?,
    val uvi: Float?
)
