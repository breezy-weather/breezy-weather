package org.breezyweather.sources.china.json

import kotlinx.serialization.Serializable

@Serializable
data class ChinaMinutelyResult(
    val precipitation: ChinaMinutelyPrecipitation? = null
)
