package org.breezyweather.weather.china.json

import kotlinx.serialization.Serializable

@Serializable
data class ChinaCurrentWind(
    val direction: org.breezyweather.weather.china.json.ChinaUnitValue?,
    val speed: org.breezyweather.weather.china.json.ChinaUnitValue?
)
