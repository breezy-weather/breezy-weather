package org.breezyweather.weather.json.metno

import kotlinx.serialization.Serializable

@Serializable
data class MetNoForecastDataInstant(
    val details: MetNoForecastDataDetails?
)
