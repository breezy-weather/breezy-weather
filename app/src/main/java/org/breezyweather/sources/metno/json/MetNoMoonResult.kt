package org.breezyweather.sources.metno.json

import kotlinx.serialization.Serializable

/**
 * MET Norway moon rise/set and phases.
 */
@Serializable
data class MetNoMoonResult(
    val properties: MetNoMoonProperties?
)
