package org.breezyweather.weather.json.mf

import kotlinx.serialization.Serializable

@Serializable
data class MfCurrentProperties(
    val gridded: MfCurrentGridded?
)
