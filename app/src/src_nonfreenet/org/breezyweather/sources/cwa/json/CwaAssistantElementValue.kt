package org.breezyweather.sources.cwa.json

import kotlinx.serialization.Serializable

@Serializable
data class CwaAssistantElementValue(
    val WeatherDescription: List<String>? = null,
)
