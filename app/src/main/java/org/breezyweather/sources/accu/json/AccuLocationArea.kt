package org.breezyweather.sources.accu.json

import kotlinx.serialization.Serializable

@Serializable
data class AccuLocationArea(
    val ID: String?,
    val LocalizedName: String,
    val EnglishName: String
)
