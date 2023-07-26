package org.breezyweather.sources.pirateweather.json

import kotlinx.serialization.Serializable

@Serializable
data class PirateWeatherForecastResult(
    val latitude: Float?,
    val longitude: Float?,
    val timezone: String?,
    val offset: Float?,
    val elevation: Int?,
    val currently: PirateWeatherCurrently?,
    val minutely: PirateWeatherForecast<PirateWeatherMinutely>,
    val hourly: PirateWeatherForecast<PirateWeatherHourly>,
    val daily: PirateWeatherForecast<PirateWeatherDaily>,
    val alerts: List<PirateWeatherAlert>,
    val flags: PirateWeatherFlags,
)
