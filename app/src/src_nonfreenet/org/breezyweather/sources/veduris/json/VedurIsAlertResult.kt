package org.breezyweather.sources.veduris.json

import kotlinx.serialization.Serializable

@Serializable
data class VedurIsAlertResult(
    val alertsByArea: Map<String, List<VedurIsAlert>>? = null,
)
