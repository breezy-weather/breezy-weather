package org.breezyweather.weather.mf.json

import kotlinx.serialization.Serializable

@Serializable
data class MfGeometry(
    val coordinates: List<Float>?
)