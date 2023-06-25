package org.breezyweather.weather.metno.json

import kotlinx.serialization.Serializable

@Serializable
data class MetNoForecastProperties(
    val meta: MetNoForecastPropertiesMeta?,
    val timeseries: List<MetNoForecastTimeseries>?
)
