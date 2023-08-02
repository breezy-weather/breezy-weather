package org.breezyweather.sources.msazure.json.hourly

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import org.breezyweather.sources.msazure.json.MsAzureWeatherUnit
import org.breezyweather.sources.msazure.json.MsAzureWind
import java.util.Date

@Serializable
data class MsAzureHourlyForecast(
    @Serializable(DateSerializer::class) @SerialName("date") val time: Date,
    val iconCode: Int?,
    @SerialName("iconPhrase") val description: String?,
    val hasPrecipitation: Boolean?,
    val isDaylight: Boolean?,
    val temperature: MsAzureWeatherUnit?,
    val realFeelTemperature: MsAzureWeatherUnit?,
    val wetBulbTemperature: MsAzureWeatherUnit?,
    val dewPoint: MsAzureWeatherUnit?,
    val wind: MsAzureWind?,
    val windGust: MsAzureWind?,
    val relativeHumidity: Int?,
    val visibility: MsAzureWeatherUnit?,
    val cloudCover: Int?,
    val ceiling: MsAzureWeatherUnit?,
    val uvIndex: Int?,
    val precipitationProbability: Int?,
    val rainProbability: Int?,
    val snowProbability: Int?,
    val iceProbability: Int?,
    val totalLiquid: MsAzureWeatherUnit?,
    val rain: MsAzureWeatherUnit?,
    val snow: MsAzureWeatherUnit?,
    val ice: MsAzureWeatherUnit?
)