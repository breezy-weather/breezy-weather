package wangdaye.com.geometricweather.weather.json.accu

import kotlinx.serialization.Serializable

@Serializable
data class AccuForecastMoon(
    val EpochRise: Long,
    val EpochSet: Long,
    val Phase: String?
)
