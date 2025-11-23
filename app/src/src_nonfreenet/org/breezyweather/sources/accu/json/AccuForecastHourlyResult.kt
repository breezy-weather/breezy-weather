/*
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

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
    val Ice: AccuValue?,
    val RelativeHumidity: Int?,
    val DewPoint: AccuValue?,
    val CloudCover: Int?,
    val Visibility: AccuValue?,
)
