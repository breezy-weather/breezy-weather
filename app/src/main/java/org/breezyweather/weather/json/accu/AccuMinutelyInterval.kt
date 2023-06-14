package org.breezyweather.weather.json.accu

import kotlinx.serialization.Serializable

@Serializable
data class AccuMinutelyInterval(
    val StartEpochDateTime: Long,
    val Minute: Int,
    val Dbz: Double,
    val ShortPhrase: String?,
    val IconCode: Int?,
    val CloudCover: Int?
)
