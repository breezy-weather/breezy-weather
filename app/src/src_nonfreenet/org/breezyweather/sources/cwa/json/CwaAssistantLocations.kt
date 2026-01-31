package org.breezyweather.sources.cwa.json

import kotlinx.serialization.Serializable

@Serializable
data class CwaAssistantLocations(
    val Location: CwaAssistantLocation? = null,
)
