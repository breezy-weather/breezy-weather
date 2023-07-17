package org.breezyweather.sources.metno.json

import kotlinx.serialization.Serializable

@Serializable
data class MetNoForecastProperties(
    val meta: MetNoForecastPropertiesMeta?,
    val timeseries: List<MetNoForecastTimeseries>?
)
