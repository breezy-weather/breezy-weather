package org.breezyweather.common.basic.wrappers

import org.breezyweather.common.basic.models.weather.AirQuality
import java.util.Date

data class AirQualityWrapper(
    val current: AirQuality? = null,
    val dailyForecast: Map<Date, AirQuality>? = null,
    val hourlyForecast: Map<Date, AirQuality>? = null
)