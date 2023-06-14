package org.breezyweather.weather.json.mf

/**
 * Mf history result.
 */
import kotlinx.serialization.Serializable

@Serializable
data class MfHistoryResult(
    val history: List<MfHistory>?
)