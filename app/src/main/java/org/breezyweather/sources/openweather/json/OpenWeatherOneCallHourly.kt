package org.breezyweather.sources.openweather.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenWeatherOneCallHourly(
    val dt: Long,
    val temp: Float?,
    @SerialName("feels_like") val feelsLike: Float?,
    val pressure: Int?,
    val humidity: Int?,
    @SerialName("dew_point") val dewPoint: Float?,
    val uvi: Float?,
    val clouds: Int?,
    val visibility: Int?,
    @SerialName("wind_speed") val windSpeed: Float?,
    @SerialName("wind_deg") val windDeg: Int?,
    val weather: List<OpenWeatherOneCallWeather>?,
    val pop: Float?,
    val rain: OpenWeatherOneCallPrecipitation?,
    val snow: OpenWeatherOneCallPrecipitation?
)
