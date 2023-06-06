package wangdaye.com.geometricweather.weather.json.openmeteo

import kotlinx.serialization.Serializable

/**
 * Open-Meteo air quality
 */
@Serializable
data class OpenMeteoAirQualityHourly(
    val time: LongArray
)
