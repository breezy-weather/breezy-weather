package org.breezyweather.weather.mf.json.atmoaura

import kotlinx.serialization.Serializable

@Serializable
data class AtmoAuraPointPolluant(
    val polluant: String?,
    val horaires: List<AtmoAuraPointHoraire>?
)
