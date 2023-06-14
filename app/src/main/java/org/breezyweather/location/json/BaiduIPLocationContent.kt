package org.breezyweather.location.json

import kotlinx.serialization.Serializable

@Serializable
data class BaiduIPLocationContent(
    val point: BaiduIPLocationContentPoint?
)
