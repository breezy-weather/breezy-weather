package org.breezyweather.sources.openmeteo.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenMeteoAirQualityHourly(
    val time: LongArray,
    val pm10: Array<Float?>?,
    @SerialName("pm2_5") val pm25: Array<Float?>?,
    @SerialName("carbon_monoxide") val carbonMonoxide: Array<Float?>?,
    @SerialName("nitrogen_dioxide") val nitrogenDioxide: Array<Float?>?,
    @SerialName("sulphur_dioxide") val sulphurDioxide: Array<Float?>?,
    val ozone: Array<Float?>?
)
