package org.breezyweather.sources.weatherbit.json

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class WeatherbitHourly(
    @SerialName("ts") val time: Long,
    @SerialName("snow") val snow: Float?,
    @SerialName("snow_depth") val snowDepth: Int?,
    @SerialName("precip") val precipitation: Float?,
    @SerialName("temp") val temperature: Float?,
    @SerialName("dewpt") val dewPoint: Float?,
    @SerialName("app_temp") val apparentTemperature: Float?,
    @SerialName("rh") val humidity: Float?,
    @SerialName("clouds") val cloudCover: Int?,
    @SerialName("weather") val weather: WeatherbitWeather?,
    @SerialName("slp") val pressure: Float?,
    @SerialName("uv") val uvIndex: Float?,
    @SerialName("vis") val visibility: Float?,
    @SerialName("pod") val timeOfDay: String?,
    @SerialName("pop") val precipitationProbability: Int?,
    @SerialName("wind_spd") val windSpeed: Float?,
    @SerialName("wind_gust_spd") val windGustSpeed: Float?,
    @SerialName("wind_dir") val windDir: Int?
)