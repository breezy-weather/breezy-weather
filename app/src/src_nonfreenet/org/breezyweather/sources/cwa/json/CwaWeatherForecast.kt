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

package org.breezyweather.sources.cwa.json

import kotlinx.serialization.Serializable

@Serializable
data class CwaWeatherForecast(
    val Wx: CwaWeatherForecastElement?,
    val T: CwaWeatherForecastElement?,
    val MinT: CwaWeatherForecastElement?,
    val MaxT: CwaWeatherForecastElement?,
    val AT: CwaWeatherForecastElement?,
    val MinAT: CwaWeatherForecastElement?,
    val MaxAT: CwaWeatherForecastElement?,
    val Td: CwaWeatherForecastElement?,
    val RH: CwaWeatherForecastElement?,
    val WD: CwaWeatherForecastElement?,
    val WS: CwaWeatherForecastElement?,
    val PoP6h: CwaWeatherForecastElement?,
    val PoP12h: CwaWeatherForecastElement?,
    val UVI: CwaWeatherForecastElement?,
)
