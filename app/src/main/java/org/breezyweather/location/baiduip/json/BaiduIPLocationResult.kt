package org.breezyweather.location.baiduip.json

import kotlinx.serialization.Serializable

@Serializable
data class BaiduIPLocationResult(
    val content: BaiduIPLocationContent?
)
