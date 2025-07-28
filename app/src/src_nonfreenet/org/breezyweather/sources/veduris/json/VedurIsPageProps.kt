package org.breezyweather.sources.veduris.json

import kotlinx.serialization.Serializable

@Serializable
data class VedurIsPageProps(
    val stationForecast: VedurIsStationForecast?,
    val latestObservation: VedurIsLatestObservation?,
)
