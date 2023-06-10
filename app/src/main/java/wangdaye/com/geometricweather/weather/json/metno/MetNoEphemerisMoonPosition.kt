package wangdaye.com.geometricweather.weather.json.metno

import kotlinx.serialization.Serializable

@Serializable
data class MetNoEphemerisMoonPosition(
    val phase: Float?,
    val desc: String?
)
