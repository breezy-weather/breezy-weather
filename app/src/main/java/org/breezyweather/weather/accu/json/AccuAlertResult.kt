package org.breezyweather.weather.accu.json

import kotlinx.serialization.Serializable

/**
 * Accu alert result.
 */
@Serializable
data class AccuAlertResult(
    val AlertID: Int,
    val Description: AccuAlertDescription?,
    val Category: String?,
    val Priority: Int,
    val Type: String?,
    val TypeID: String?,
    val Level: String?,
    val Source: String?,
    val SourceId: Int,
    val Area: List<AccuAlertArea>?
)
