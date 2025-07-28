package org.breezyweather.sources.veduris.json

import kotlinx.serialization.Serializable

@Serializable
data class VedurIsStationResult(
    val forecasts: Map<String, VedurIsForecast>? = null,
)
