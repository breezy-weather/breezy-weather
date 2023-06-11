package wangdaye.com.geometricweather.location.json

import kotlinx.serialization.Serializable

@Serializable
data class BaiduIPLocationContent(
    val point: BaiduIPLocationContentPoint?
)
