package org.breezyweather.sources.here.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import java.util.Date

@Serializable
data class HereWeatherData(
    @Serializable(DateSerializer::class) val time: Date,
    val weekday: String?,
    val daySegment: String?,
    val skyDesc: String?,
    val temperature: Float?,
    val comfort: String?,
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
    val barometerPressure: Float?,
    val visibility: Float?,
    val snowCover: Float?,
    val iconId: Int?
)