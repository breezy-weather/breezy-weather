package org.breezyweather.sources.metno.json

import kotlinx.serialization.Serializable

@Serializable
data class MetNoNowcastProperties(
    val timeseries: List<MetNoForecastTimeseries>?
)
