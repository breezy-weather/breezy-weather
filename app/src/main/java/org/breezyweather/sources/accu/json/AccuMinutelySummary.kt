package org.breezyweather.sources.accu.json

import kotlinx.serialization.Serializable

@Serializable
data class AccuMinutelySummary(
    val LongPhrase: String?
)
