package wangdaye.com.geometricweather.weather.json.owm

import kotlinx.serialization.Serializable

@Serializable
data class OwmAirPollution(
    val dt: Long,
    val components: OwmAirPollutionComponents?,
)
