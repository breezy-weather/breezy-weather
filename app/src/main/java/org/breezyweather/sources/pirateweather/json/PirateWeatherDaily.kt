package org.breezyweather.sources.pirateweather.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PirateWeatherDaily(
    val time: Long,
    val icon: String?,
    val summary: String?,

    @SerialName("sunriseTime") val sunrise: Long?,
    @SerialName("sunsetTime") val sunset: Long?,
    val moonPhase: Float?,

    val precipType: String?,
    val precipIntensity: Float?,
    val precipIntensityMax: Float?,
    val precipIntensityMaxTime: Long?,
    val precipProbability: Float?,
    val precipAccumulation: Float?,

    val temperatureHigh: Float?,
    val temperatureHighTime: Long?,
    val temperatureLow: Float?,
    val temperatureLowTime: Long?,
    val apparentTemperatureHigh: Float?,
    val apparentTemperatureHighTime: Long?,
    val apparentTemperatureLow: Float?,
    val apparentTemperatureLowTime: Long?,

    val temperatureMin: Float?,
    val temperatureMinTime: Long?,
    val temperatureMax: Float?,
    val temperatureMaxTime: Long?,
    val apparentTemperatureMin: Float?,
    val apparentTemperatureMinTime: Long?,
    val apparentTemperatureMax: Float?,
    val apparentTemperatureMaxTime: Long?,

    val dewPoint: Float?,
    val humidity: Float?,
    val pressure: Float?,
    val windSpeed: Float?,
    val windGust: Float?,
    val windGustTime: Long?,
    val windBearing: Float?,
    val cloudCover: Float?,
    val uvIndex: Float?,
    val uvIndexTime: Long?,
    val visibility: Float?
)
