package org.breezyweather.sources.pirateweather.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PirateWeatherFlagsSourceTimes(
    @SerialName("hrrr_0-18") val hrrr_0_18: String?,
    val hrrr_subh: String?,
    @SerialName("hrrr_18-48") val hrrr_18_48: String?,
    val gfs: String?,
    val gefs: String?
)
