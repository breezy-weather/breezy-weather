package org.breezyweather.sources.msazure.json.daily

import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import org.breezyweather.sources.msazure.json.MsAzureTemperatureRange
import java.util.Date

@Serializable
data class MsAzureDailyForecast(
    @Serializable(DateSerializer::class) val date: Date,
    val temperature: MsAzureTemperatureRange?,
    val realFeelTemperature: MsAzureTemperatureRange?,
    val realFeelTemperatureShade: MsAzureTemperatureRange?,
    val hoursOfSun: Double?,
    val degreeDaySummary: MsAzureDegreeDay?,
    val airAndPollen: List<MsAzureWeatherAirAndPollen>?,
    val day: MsAzureTimeOfDay?,
    val night: MsAzureTimeOfDay?
)