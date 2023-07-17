package org.breezyweather.sources.accu.json

import kotlinx.serialization.Serializable

/**
 * Accu hourly result.
 */
@Serializable
data class AccuForecastHourlyResult(
    val EpochDateTime: Long,
    val WeatherIcon: Int?,
    val IconPhrase: String?,
    val IsDaylight: Boolean,
    val Temperature: AccuValue?,
    val RealFeelTemperature: AccuValue?,
    val RealFeelTemperatureShade: AccuValue?,
    val WetBulbTemperature: AccuValue?,
    val PrecipitationProbability: Int?,
    val ThunderstormProbability: Int?,
    val RainProbability: Int?,
    val SnowProbability: Int?,
    val IceProbability: Int?,
    val Wind: AccuForecastWind?,
    val WindGust: AccuForecastWind?,
    val UVIndex: Int?,
    val UVIndexText: String?,
    val TotalLiquid: AccuValue?,
    val Rain: AccuValue?,
    val Snow: AccuValue?,
    val Ice: AccuValue?
)