package org.breezyweather.weather.metno.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MetNoForecastDataSummary(
    @SerialName("symbol_code") val symbolCode: String?
)
