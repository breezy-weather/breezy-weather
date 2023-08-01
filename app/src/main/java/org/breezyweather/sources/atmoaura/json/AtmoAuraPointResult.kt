package org.breezyweather.sources.atmoaura.json

import kotlinx.serialization.Serializable

/**
 * Atmo Aura
 */
@Serializable
data class AtmoAuraPointResult(
    val polluants: List<AtmoAuraPointPolluant>? = null
)