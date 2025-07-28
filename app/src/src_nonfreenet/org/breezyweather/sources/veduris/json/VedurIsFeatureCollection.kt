package org.breezyweather.sources.veduris.json

import kotlinx.serialization.Serializable

@Serializable
data class VedurIsFeatureCollection(
    val features: List<VedurIsFeature>?,
)
