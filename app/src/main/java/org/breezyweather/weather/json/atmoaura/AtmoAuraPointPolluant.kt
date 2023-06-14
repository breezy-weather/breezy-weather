package org.breezyweather.weather.json.atmoaura

import kotlinx.serialization.Serializable

@Serializable
data class AtmoAuraPointPolluant(
    val polluant: String?,
    val horaires: List<AtmoAuraPointHoraire>?
)
