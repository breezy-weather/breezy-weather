package wangdaye.com.geometricweather.weather.json.openweather

import kotlinx.serialization.Serializable

/**
 * OpenWeather Air Pollution result.
 */
@Serializable
data class OpenWeatherAirPollutionResult(
    val list: List<OpenWeatherAirPollution>?,
)
