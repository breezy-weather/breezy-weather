package org.breezyweather.weather.json.accu

import kotlinx.serialization.Serializable

@Serializable
data class AccuLocationArea(
    val ID: String?,
    val LocalizedName: String
)
