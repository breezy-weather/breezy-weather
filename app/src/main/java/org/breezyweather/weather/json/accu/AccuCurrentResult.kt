package org.breezyweather.weather.json.accu

import kotlinx.serialization.Serializable

/**
 * Accu realtime result.
 */
@Serializable
data class AccuCurrentResult(
    val EpochTime: Long,
    val WeatherText: String?,
    val WeatherIcon: Int?,
    val Temperature: AccuValueContainer?,
    val RealFeelTemperature: AccuValueContainer?,
    val RealFeelTemperatureShade: AccuValueContainer?,
    val RelativeHumidity: Int?,
    val DewPoint: AccuValueContainer?,
    val Wind: AccuCurrentWind?,
    val WindGust: AccuCurrentWindGust?,
    val UVIndex: Int?,
    val UVIndexText: String?,
    val Visibility: AccuValueContainer?,
    val CloudCover: Int?,
    val Ceiling: AccuValueContainer?,
    val Pressure: AccuValueContainer?,
    val ApparentTemperature: AccuValueContainer?,
    val WindChillTemperature: AccuValueContainer?,
    val WetBulbTemperature: AccuValueContainer?,
    val PrecipitationSummary: AccuCurrentPrecipitationSummary?,
    val TemperatureSummary: AccuCurrentTemperatureSummary?
)
