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
    val ozone: Array<Float?>?,
    @SerialName("alder_pollen") val alderPollen: Array<Float?>?,
    @SerialName("birch_pollen") val birchPollen: Array<Float?>?,
    @SerialName("grass_pollen") val grassPollen: Array<Float?>?,
    @SerialName("mugwort_pollen") val mugwortPollen: Array<Float?>?,
    @SerialName("olive_pollen") val olivePollen: Array<Float?>?,
    @SerialName("ragweed_pollen") val ragweedPollen: Array<Float?>?
)
