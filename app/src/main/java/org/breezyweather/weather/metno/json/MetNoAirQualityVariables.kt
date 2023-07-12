package org.breezyweather.weather.metno.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MetNoAirQualityVariables(
    @SerialName("no2_concentration") val no2Concentration: MetNoAirQualityConcentration?,
    @SerialName("pm10_concentration") val pm10Concentration: MetNoAirQualityConcentration?,
    @SerialName("pm25_concentration") val pm25Concentration: MetNoAirQualityConcentration?,
    @SerialName("o3_concentration") val o3Concentration: MetNoAirQualityConcentration?,
    @SerialName("so2_concentration") val so2Concentration: MetNoAirQualityConcentration?
)
