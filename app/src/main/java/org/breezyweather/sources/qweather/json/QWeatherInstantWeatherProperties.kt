package org.breezyweather.sources.qweather.json

import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import java.util.Date

@Serializable
data class QWeatherInstantWeatherProperties(
    @Serializable(DateSerializer::class) val fxTime: Date?,
    val temp: String,
    val feelsLike: String?,
    val icon: String,
    val text: String,
    val windDir: String,
    val wind360: String,
    val windScale: String,
    val windSpeed: String,
    val humidity: String,
    val pop: String?,
    val precip: String,
    val pressure: String,
    val vis: String?,
    val cloud: String?,
    val dew: String?,
)
