package org.breezyweather.weather.json.accu

import kotlinx.serialization.Serializable

@Serializable
data class AccuAlertArea(
    val EpochStartTime: Long,
    val EpochEndTime: Long,
    val Text: String?
)
