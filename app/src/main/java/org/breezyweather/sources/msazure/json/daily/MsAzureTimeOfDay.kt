package org.breezyweather.sources.msazure.json.daily

import kotlinx.serialization.Serializable
import org.breezyweather.sources.msazure.json.MsAzureWeatherUnit
import org.breezyweather.sources.msazure.json.MsAzureWind

@Serializable
data class MsAzureTimeOfDay(
    val iconCode: Int?,
    val hasPrecipitation: Boolean?,
    val precipitationType: String?,
    val shortPhrase: String?,
    val longPhrase: String?,
    val precipitationProbability: Int?,
    val thunderstormProbability: Int?,
    val rainProbability: Int?,
    val snowProbability: Int?,
    val iceProbability: Int?,
    val wind: MsAzureWind?,
    val windGust: MsAzureWind?,
    val totalLiquid: MsAzureWeatherUnit?,
    val rain: MsAzureWeatherUnit?,
    val snow: MsAzureWeatherUnit?,
    val ice: MsAzureWeatherUnit?,
    val hoursOfPrecipitation: Double?,
    val hoursOfRain: Double?,
    val hoursOfSnow: Double?,
    val hoursOfIce: Double?,
    val cloudCover: Int?
)