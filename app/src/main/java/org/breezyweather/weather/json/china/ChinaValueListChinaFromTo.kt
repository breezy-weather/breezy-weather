package org.breezyweather.weather.json.china

import kotlinx.serialization.Serializable

@Serializable
data class ChinaValueListChinaFromTo(
    val value: List<ChinaFromTo>?
)
