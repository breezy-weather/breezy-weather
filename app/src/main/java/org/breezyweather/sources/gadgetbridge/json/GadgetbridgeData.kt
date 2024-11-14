/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.sources.gadgetbridge.json

import kotlinx.serialization.Serializable

/**
 * Refer to
 * https://codeberg.org/Freeyourgadget/Gadgetbridge/src/branch/master/app/src/main/java/nodomain/freeyourgadget/gadgetbridge/model/WeatherSpec.java
 */
@Serializable
data class GadgetbridgeData(
    val timestamp: Int? = null,
    val location: String? = null,
    val currentTemp: Int? = null,
    val currentConditionCode: Int? = null,
    val currentCondition: String? = null,
    val currentHumidity: Int? = null,
    val todayMaxTemp: Int? = null,
    val todayMinTemp: Int? = null,
    val windSpeed: Float? = null,
    val windDirection: Int? = null,
    val uvIndex: Float? = null,
    val precipProbability: Int? = null,
    val dewPoint: Int? = null,
    val pressure: Float? = null,
    val cloudCover: Int? = null,
    val visibility: Float? = null,
    val sunRise: Int? = null,
    val sunSet: Int? = null,
    val moonRise: Int? = null,
    val moonSet: Int? = null,
    val moonPhase: Int? = null,
    val feelsLikeTemp: Int? = null,
    val forecasts: List<GadgetbridgeDailyForecast>? = null,
    val hourly: List<GadgetbridgeHourlyForecast>? = null,
    val airQuality: GadgetbridgeAirQuality? = null,
)
