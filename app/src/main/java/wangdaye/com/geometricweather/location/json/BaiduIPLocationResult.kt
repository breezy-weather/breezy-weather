package wangdaye.com.geometricweather.location.json

import kotlinx.serialization.Serializable

@Serializable
data class BaiduIPLocationResult(
    val content: BaiduIPLocationContent?
)
