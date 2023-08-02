package org.breezyweather.sources.msazure.json.current

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import org.breezyweather.sources.msazure.json.MsAzureWeatherUnit
import org.breezyweather.sources.msazure.json.MsAzureWind
import java.util.Date

@Serializable
data class MsAzureCurrentConditions(
    @Serializable(DateSerializer::class) @SerialName("dateTime") val time: Date,
    @SerialName("phrase") val description: String?,
    val iconCode: Int?,
    val hasPrecipitation: Boolean?,
    val isDayTime: Boolean?,
    val temperature: MsAzureWeatherUnit?,
    val realFeelTemperature: MsAzureWeatherUnit?,
    val realFeelTemperatureShade: MsAzureWeatherUnit?,
    val relativeHumidity: Double?,
    val dewPoint: MsAzureWeatherUnit?,
    val wind: MsAzureWind?,
    val windGust: MsAzureWind?,
    val uvIndex: Int?,
    val visibility: MsAzureWeatherUnit?,
    val cloudCover: Int?,
    val ceiling: MsAzureWeatherUnit?,
    val pressure: MsAzureWeatherUnit?,
    val apparentTemperature: MsAzureWeatherUnit?,
    val windChillTemperature: MsAzureWeatherUnit?,
    val wetBulbTemperature: MsAzureWeatherUnit?,
    @SerialName("temperatureSummary") val pastTemperature: MsAzurePastTemperature?
)
