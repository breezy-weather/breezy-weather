package org.breezyweather.weather.china.json

import kotlinx.serialization.Serializable

@Serializable
data class ChinaPrecipitationProbability(
    val value: List<String>?
)
