/**
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
    val TemperatureSummary: AccuCurrentTemperatureSummary?,
)
