package org.breezyweather.sources.veduris.json

import kotlinx.serialization.Serializable

@Serializable
data class VedurIsStation(
    val id: Long,
    val name: String,
    val displayName: String,
    val isVirtual: Boolean,
)
