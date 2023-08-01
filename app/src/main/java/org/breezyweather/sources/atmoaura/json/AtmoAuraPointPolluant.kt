package org.breezyweather.sources.atmoaura.json

import kotlinx.serialization.Serializable

@Serializable
data class AtmoAuraPointPolluant(
    val polluant: String?,
    val horaires: List<AtmoAuraPointHoraire>?
)
