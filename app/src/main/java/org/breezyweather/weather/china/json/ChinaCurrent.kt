package org.breezyweather.weather.china.json

import java.util.*

import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer

@Serializable
data class ChinaCurrent(
    @Serializable(DateSerializer::class) val pubTime: Date,
    val feelsLike: org.breezyweather.weather.china.json.ChinaUnitValue?,
    val humidity: org.breezyweather.weather.china.json.ChinaUnitValue?,
    val pressure: org.breezyweather.weather.china.json.ChinaUnitValue?,
    val temperature: org.breezyweather.weather.china.json.ChinaUnitValue?,
    val uvIndex: String?,
    val visibility: org.breezyweather.weather.china.json.ChinaUnitValue?,
    val weather: String?,
    val wind: org.breezyweather.weather.china.json.ChinaCurrentWind?
)
