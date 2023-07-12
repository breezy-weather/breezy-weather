package org.breezyweather.weather.metno.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MetNoForecastData(
    val instant: MetNoForecastDataInstant?,
    @SerialName("next_12_hours") val next12Hours: MetNoForecastDataNextHours?,
    @SerialName("next_1_hours") val next1Hours: MetNoForecastDataNextHours?,
    @SerialName("next_6_hours") val next6Hours: MetNoForecastDataNextHours?
) {
    val symbolCode: String?
        get() = next1Hours?.summary?.symbolCode
            ?: next6Hours?.summary?.symbolCode
            ?: next12Hours?.summary?.symbolCode
}
