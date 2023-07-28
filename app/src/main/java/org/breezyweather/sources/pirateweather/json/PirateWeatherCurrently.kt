package org.breezyweather.sources.pirateweather.json

import kotlinx.serialization.Serializable

@Serializable
data class PirateWeatherCurrently(
    val time: Long,
    val icon: String?,
    val summary: String?,

    val nearestStormDistance: Int?,
    val nearestStormBearing: Int?,

    val precipType: String?,
    val precipIntensity: Float?,
    val precipProbability: Float?,
    val precipIntensityError: Float?,

    val temperature: Float?,
    val apparentTemperature: Float?,

    val dewPoint: Float?,
    val humidity: Float?,
    val pressure: Float?,
    val windSpeed: Float?,
    val windGust: Float?,
    val windBearing: Float?,
    val cloudCover: Float?,
    val uvIndex: Float?,
    val visibility: Float?
)
