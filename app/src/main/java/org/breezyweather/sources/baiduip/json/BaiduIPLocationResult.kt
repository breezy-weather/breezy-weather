package org.breezyweather.sources.baiduip.json

import kotlinx.serialization.Serializable

@Serializable
data class BaiduIPLocationResult(
    val status: Int,
    val content: BaiduIPLocationContent?
)
