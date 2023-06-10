package wangdaye.com.geometricweather.weather.json.owm

import kotlinx.serialization.Serializable

@Serializable
data class OwmAirPollutionComponents(
    val co: Float?,
    val no: Float?,
    val no2: Float?,
    val o3: Float?,
    val so2: Float?,
    val pm2_5: Float?,
    val pm10: Float?,
    val nh3: Float?
)