package org.breezyweather.weather.json.china

import kotlinx.serialization.Serializable

@Serializable
data class ChinaCurrentWind(
    val direction: ChinaUnitValue?,
    val speed: ChinaUnitValue?
)
