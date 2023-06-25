package org.breezyweather.weather.metno.json

import kotlinx.serialization.Serializable

@Serializable
data class MetNoForecastDataInstant(
    val details: MetNoForecastDataDetails?
)
