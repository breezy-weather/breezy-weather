package org.breezyweather.weather.json.openmeteo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenMeteoWeatherDaily(
    val time: LongArray,
    @SerialName("temperature_2m_max") val temperatureMax: Array<Float?>?,
    @SerialName("temperature_2m_min") val temperatureMin: Array<Float?>?,
    @SerialName("apparent_temperature_max") val apparentTemperatureMax: Array<Float?>?,
    @SerialName("apparent_temperature_min") val apparentTemperatureMin: Array<Float?>?,
    val sunrise: Array<Long?>?,
    val sunset: Array<Long?>?,
    @SerialName("uv_index_max") val uvIndexMax: Array<Float?>?
)