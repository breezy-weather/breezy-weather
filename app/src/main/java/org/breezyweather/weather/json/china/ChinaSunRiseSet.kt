package org.breezyweather.weather.json.china

import kotlinx.serialization.Serializable

@Serializable
data class ChinaSunRiseSet(
    val value: List<ChinaSunRiseSetValue>?
)
