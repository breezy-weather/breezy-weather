package org.breezyweather.sources.china.json

import kotlinx.serialization.Serializable

@Serializable
data class ChinaValueListChinaFromTo(
    val value: List<ChinaFromTo>?
)
