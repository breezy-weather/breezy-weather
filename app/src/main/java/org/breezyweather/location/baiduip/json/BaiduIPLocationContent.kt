package org.breezyweather.location.baiduip.json

import kotlinx.serialization.Serializable

@Serializable
data class BaiduIPLocationContent(
    val point: BaiduIPLocationContentPoint?
)
