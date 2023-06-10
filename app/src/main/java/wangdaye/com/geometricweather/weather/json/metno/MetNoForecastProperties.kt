package wangdaye.com.geometricweather.weather.json.metno

import kotlinx.serialization.Serializable

@Serializable
data class MetNoForecastProperties(
    val meta: MetNoForecastPropertiesMeta?,
    val timeseries: List<MetNoForecastTimeseries>?
)
