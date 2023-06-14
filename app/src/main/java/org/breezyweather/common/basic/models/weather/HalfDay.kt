package org.breezyweather.common.basic.models.weather

import java.io.Serializable

/**
 * Half day.
 */
class HalfDay(
    val weatherText: String? = null,
    val weatherPhase: String? = null,
    val weatherCode: WeatherCode? = null,
    val temperature: Temperature? = null,
    val precipitation: Precipitation? = null,
    val precipitationProbability: PrecipitationProbability? = null,
    val precipitationDuration: PrecipitationDuration? = null,
    val wind: Wind? = null,
    val cloudCover: Int? = null
) : Serializable 