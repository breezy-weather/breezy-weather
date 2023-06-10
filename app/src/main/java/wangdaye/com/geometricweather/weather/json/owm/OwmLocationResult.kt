package wangdaye.com.geometricweather.weather.json.owm

import kotlinx.serialization.Serializable

/**
 * OpenWeather location result.
 */
@Serializable
data class OwmLocationResult(
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String
)
