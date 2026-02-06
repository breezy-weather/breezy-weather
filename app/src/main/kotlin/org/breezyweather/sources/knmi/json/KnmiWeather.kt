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

package org.breezyweather.sources.knmi.json

import kotlinx.serialization.Serializable

@Serializable
data class KnmiWeather(
    val backgrounds: List<KnmiWeatherBackground>? = null,
    val summaries: List<KnmiWeatherSummary>? = null,
    val alerts: List<KnmiWeatherAlert>? = null,
    val hourly: KnmiHourlyWeatherForecast? = null,
    val daily: KnmiDailyWeatherForecast? = null,
    val sun: KnmiSunData? = null,
    val wind: KnmiWind? = null,
    val uvIndex: KnmiUvIndex? = null,
)
