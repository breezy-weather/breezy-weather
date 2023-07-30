package org.breezyweather.common.basic.models.weather

import java.io.Serializable

/**
 * Current.
 *
 * default unit
 * [.relativeHumidity] : [RelativeHumidityUnit.PERCENT]
 * [.dewPoint] : [TemperatureUnit.C]
 * [.visibility] : [DistanceUnit.M]
 * [.ceiling] : [DistanceUnit.M]
 */
data class Current(
    val weatherText: String? = null,
    val weatherCode: WeatherCode? = null,
    val temperature: Temperature? = null,
    val wind: Wind? = null,
    val uV: UV? = null,
    val airQuality: AirQuality? = null,
    val relativeHumidity: Float? = null,
    val dewPoint: Float? = null,
    val pressure: Float? = null,
    val cloudCover: Int? = null,
    val visibility: Float? = null,
    val ceiling: Float? = null,
    val dailyForecast: String? = null,
    val hourlyForecast: String? = null
) : Serializable
