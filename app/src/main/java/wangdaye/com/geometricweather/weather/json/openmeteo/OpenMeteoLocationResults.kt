package wangdaye.com.geometricweather.weather.json.openmeteo

import kotlinx.serialization.Serializable

/**
 * Open Meteo geocoding
 */
@Serializable
data class OpenMeteoLocationResults(
    val results: List<OpenMeteoLocationResult>?
)