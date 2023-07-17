package org.breezyweather.sources.mf.json

import kotlinx.serialization.Serializable

@Serializable
data class MfCurrentProperties(
    val gridded: MfCurrentGridded?
)
