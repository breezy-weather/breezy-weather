package org.breezyweather.sources.qweather.json

import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import java.util.Date

@Serializable
data class QWeatherMinuelyPrecipitationProperties(
    @Serializable(DateSerializer::class) val fxTime: Date,
    val precip: String,
    val type: String,
)
