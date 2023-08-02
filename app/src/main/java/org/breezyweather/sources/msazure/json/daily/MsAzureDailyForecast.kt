package org.breezyweather.sources.msazure.json.daily

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import org.breezyweather.sources.msazure.json.MsAzureTemperatureRange
import java.util.Date

@Serializable
data class MsAzureDailyForecast(
    @Serializable(DateSerializer::class) @SerialName("date") val time: Date,
    val temperature: MsAzureTemperatureRange?,
    val realFeelTemperature: MsAzureTemperatureRange?,
    val realFeelTemperatureShade: MsAzureTemperatureRange?,
    val hoursOfSun: Double?,
    @SerialName("degreeDaySummary") val degreeDay: MsAzureDegreeDay?,
    @SerialName("airAndPollen") val airTraits: List<MsAzureWeatherAirTraits>?,
    val day: MsAzureTimeOfDay?,
    val night: MsAzureTimeOfDay?
)