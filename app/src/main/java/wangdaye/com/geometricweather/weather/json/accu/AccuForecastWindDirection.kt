package wangdaye.com.geometricweather.weather.json.accu

import kotlinx.serialization.Serializable

@Serializable
data class AccuForecastWindDirection(
    val Degrees: Int?,
    val Localized: String?
)
