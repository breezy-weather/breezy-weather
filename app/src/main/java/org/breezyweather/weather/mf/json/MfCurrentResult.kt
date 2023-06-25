package org.breezyweather.weather.mf.json

import kotlinx.serialization.Serializable

/**
 * Mf current result.
 */
@Serializable
data class MfCurrentResult(
    val properties: MfCurrentProperties?
)