package org.breezyweather.sources.here.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HereWeatherData(
    val time: String,
    val weekday: String?,
    @SerialName("daySegment") val timeOfDay: String?,
    val skyDesc: String?,
    val temperature: Float?,
    @SerialName("comfort") val apparentTemperature: String?,
    val highTemperature: String?,
    val lowTemperature: String?,
    val humidity: String?,
    val dewPoint: Float?,
    val precipitation1H: Float?,
    val precipitation12H: Float?,
    val precipitation24H: Float?,
    val precipitationProbability: Int?,
    val precipitationDesc: String?,
    val rainFall: Float?,
    val snowFall: Float?,
    val airInfo: Int?,
    val windSpeed: Float?,
    val windDirection: Float?,
    val uvIndex: Int?,
    @SerialName("barometerPressure") val pressure: Float?,
    val visibility: Float?,
    val snowCover: Float?,
    val iconId: Int?
)