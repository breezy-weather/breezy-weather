package wangdaye.com.geometricweather.weather.json.mf

import kotlinx.serialization.Serializable

@Serializable
data class MfGeometry(
    val coordinates: List<Float>?
)