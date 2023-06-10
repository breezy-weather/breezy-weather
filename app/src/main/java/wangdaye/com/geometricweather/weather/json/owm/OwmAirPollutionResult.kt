package wangdaye.com.geometricweather.weather.json.owm

import kotlinx.serialization.Serializable

/**
 * OpenWeather Air Pollution result.
 */
@Serializable
data class OwmAirPollutionResult(
    val list: List<OwmAirPollution>?,
)
