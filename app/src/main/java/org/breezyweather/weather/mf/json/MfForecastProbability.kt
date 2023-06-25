package org.breezyweather.weather.mf.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import java.util.Date

@Serializable
data class MfForecastProbability(
    @Serializable(DateSerializer::class) val time: Date,
    @SerialName("rain_hazard_3h") val rainHazard3h: Int?,
    @SerialName("rain_hazard_6h") val rainHazard6h: Int?,
    @SerialName("snow_hazard_3h") val snowHazard3h: Int?,
    @SerialName("snow_hazard_6h") val snowHazard6h: Int?,
    @SerialName("storm_hazard") val stormHazard: Int?,
    @SerialName("freezing_hazard") val freezingHazard: Int?
)
