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

package org.breezyweather.sources.gadgetbridge.json

import kotlinx.serialization.Serializable

@Serializable
data class GadgetbridgeDailyForecast(
    val minTemp: Int? = null,
    val maxTemp: Int? = null,
    val conditionCode: Int? = null,
    val humidity: Int? = null,
    val windSpeed: Float? = null,
    val windDirection: Int? = null,
    val uvIndex: Float? = null,
    val precipProbability: Int? = null,
    val sunRise: Int? = null,
    val sunSet: Int? = null,
    val moonRise: Int? = null,
    val moonSet: Int? = null,
    val moonPhase: Int? = null,
    val airQuality: GadgetbridgeAirQuality? = null,
)
