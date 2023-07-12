package org.breezyweather.weather.metno.json

import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import java.util.Date

@Serializable
data class MetNoAirQualityTime(
    @Serializable(DateSerializer::class) val from: Date,
    @Serializable(DateSerializer::class) val to: Date,
    val variables: MetNoAirQualityVariables?
)
