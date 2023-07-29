package org.breezyweather.sources.weatherbit.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherbitCurrent(
    @SerialName("ts") val time: Long,
    @SerialName("timezone") val timezone: String?,
    @SerialName("vis") val visibility: Float?,
    @SerialName("rh") val humidity: Int?,
    @SerialName("dewpt") val dewPoint: Float?,
    @SerialName("wind_dir") val windDir: Int?,
    @SerialName("wind_spd") val windSpeed: Float?,
    @SerialName("gust") val windGustSpeed: Float?,
    @SerialName("temp") val temperature: Float?,
    @SerialName("app_temp") val apparentTemperature: Float?,
    @SerialName("clouds") val cloudCover: Int?,
    @SerialName("weather") val weather: WeatherbitWeather?,
    @SerialName("sunrise") val sunrise: String?,
    @SerialName("sunset") val sunset: String?,
    @SerialName("slp") val pressure: Float?,
    @SerialName("aqi") val aqIndex: Int?,
    @SerialName("uv") val uvIndex: Float?,
    @SerialName("precip") val precipitation: Float?,
    @SerialName("snow") val snow: Int?
)