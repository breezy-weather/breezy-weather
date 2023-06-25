package org.breezyweather.weather.mf.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import java.util.Date

@Serializable
data class MfEphemeris(
    @SerialName("moonrise_time") @Serializable(DateSerializer::class) val moonriseTime: Date?,
    @SerialName("moonset_time") @Serializable(DateSerializer::class) val moonsetTime: Date?,
    @SerialName("moon_phase") val moonPhase: String?,
    @SerialName("moon_phase_description") val moonPhaseDescription: String?
)
