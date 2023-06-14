package org.breezyweather.weather.json.china

import kotlinx.serialization.Serializable

@Serializable
data class ChinaPrecipitationProbability(
    val value: List<String>?
)
