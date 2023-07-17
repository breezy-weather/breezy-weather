package org.breezyweather.sources.mf.json.atmoaura

import kotlinx.serialization.Serializable

/**
 * Atmo Aura
 */
@Serializable
data class AtmoAuraPointResult(
    val polluants: List<AtmoAuraPointPolluant>? = null
)