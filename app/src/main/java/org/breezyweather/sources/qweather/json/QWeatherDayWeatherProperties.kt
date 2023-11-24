package org.breezyweather.sources.qweather.json

import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import java.util.Date

@Serializable
data class QWeatherDayWeatherProperties(
    @Serializable(DateSerializer::class) val fxDate: Date,
    val sunrise: String?,
    val sunset: String?,
    val moonrise: String?,
    val moonset: String?,
    val moonPhase: String,
    val moonPhaseIcon: String,
    val tempMax: String,
    val tempMin: String,
    val iconDay: String,
    val textDay: String,
    val iconNight: String,
    val textNight: String,
    val windDirDay: String,
    val wind360Day: String,
    val windScaleDay: String,
    val windSpeedDay: String,
    val windDirNight: String,
    val wind360Night: String,
    val windScaleNight: String,
    val windSpeedNight: String,
    val humidity: String,
    val precip: String,
    val pressure: String,
    val vis: String,
    val cloud: String,
)
