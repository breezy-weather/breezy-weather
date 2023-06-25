package org.breezyweather.weather.china.json

import java.util.*

import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer

@Serializable
data class ChinaSunRiseSetValue(
    @Serializable(DateSerializer::class) val from: Date,
    @Serializable(DateSerializer::class) val to: Date
)
