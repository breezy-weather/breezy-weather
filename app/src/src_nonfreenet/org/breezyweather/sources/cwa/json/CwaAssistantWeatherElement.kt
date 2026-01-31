package org.breezyweather.sources.cwa.json

import kotlinx.serialization.Serializable

@Serializable
data class CwaAssistantWeatherElement(
    val ElementValue: CwaAssistantElementValue? = null,
)
