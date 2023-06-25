package org.breezyweather.weather.openmeteo.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenMeteoWeatherHourly(
    val time: LongArray,
    @SerialName("temperature_2m") val temperature: Array<Float?>?,
    @SerialName("apparent_temperature") val apparentTemperature: Array<Float?>?,
    @SerialName("precipitation_probability") val precipitationProbability: Array<Int?>?,
    val precipitation: Array<Float?>?,
    val rain: Array<Float?>?,
    val showers: Array<Float?>?,
    val snowfall: Array<Float?>?,
    @SerialName("weathercode") val weatherCode: Array<Int?>?,
    @SerialName("windspeed_10m") val windSpeed: Array<Float?>?,
    @SerialName("winddirection_10m") val windDirection: Array<Int?>?,
    @SerialName("uv_index") val uvIndex: Array<Float?>?,
    @SerialName("is_day") val isDay: IntArray?, /* Should be a boolean (true or false) but API returns an integer */
    // Below are used in current only
    @SerialName("relativehumidity_2m") val relativeHumidity: Array<Int?>?,
    @SerialName("dewpoint_2m") val dewPoint: Array<Float?>?,
    @SerialName("surface_pressure") val surfacePressure: Array<Float?>?,
    @SerialName("cloudcover") val cloudCover: Array<Int?>?,
    val visibility: Array<Float?>?
)