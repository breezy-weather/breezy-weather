package org.breezyweather.sources.here.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents *possible* reported categories
 * Is either empty or contains 1 category
 */
@Serializable
data class HereWeatherPlaceReport(
    val observations: List<HereWeatherData>?,
    val dailyForecasts: List<HereWeatherForecasts<HereWeatherData>>?,
    val hourlyForecasts: List<HereWeatherForecasts<HereWeatherData>>?,
    val astronomyForecasts: List<HereWeatherForecasts<HereWeatherAstronomy>>?,
    val nwsAlerts: HereWeatherNWSAlerts?
)
