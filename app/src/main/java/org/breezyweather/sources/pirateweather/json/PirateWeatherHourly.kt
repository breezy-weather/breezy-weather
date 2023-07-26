package org.breezyweather.sources.pirateweather.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PirateWeatherHourly(
    val time: Long,
    val icon: String?,
    val summary: String?,

    val precipType: String?,
    val precipIntensity: Float?,
    val precipProbability: Float?,
    val precipIntensityError: Float?,
    val precipAccumulation: Float?,

    val temperature: Float?,
    val apparentTemperature: Float?,
    val dewPoint: Float?,
    val humidity: Float?,
    val pressure: Float?,
    val windSpeed: Float?,
    val windGust: Float?,
    val windBearing: Float?,
    val uvIndex: Float?,
    val cloudCover: Float?,
    val visibility: Float?,

    val ozone: Float?
)
