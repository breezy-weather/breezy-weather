package org.breezyweather.weather.mf.json

import kotlinx.serialization.Serializable

@Serializable
data class MfCurrentProperties(
    val gridded: MfCurrentGridded?
)
