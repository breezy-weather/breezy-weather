package wangdaye.com.geometricweather.weather.json.accu

import kotlinx.serialization.Serializable

@Serializable
data class AccuLocationGeoPosition(
    val Latitude: Double,
    val Longitude: Double
)
