package org.breezyweather.weather.json.metno

import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import java.util.*

@Serializable
data class MetNoForecastTimeseries(
    @Serializable(DateSerializer::class) val time: Date,
    val data: MetNoForecastData?
)
