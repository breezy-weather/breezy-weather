package org.breezyweather.weather.china.json

import kotlinx.serialization.Serializable

@Serializable
data class ChinaCurrentWind(
    val direction: ChinaUnitValue?,
    val speed: ChinaUnitValue?
)
