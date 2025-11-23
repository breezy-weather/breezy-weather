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

package org.breezyweather.sources.aemet.json

import kotlinx.serialization.Serializable

@Serializable
data class AemetHourlyDay(
    val fecha: String,
    val orto: String?,
    val ocaso: String?,
    val estadoCielo: List<AemetForecastData>?,
    val precipitacion: List<AemetForecastData>?,
    val probPrecipitacion: List<AemetForecastData>?,
    val probTormenta: List<AemetForecastData>?,
    val nieve: List<AemetForecastData>?,
    val probNieve: List<AemetForecastData>?,
    val temperatura: List<AemetForecastData>?,
    val sensTermica: List<AemetForecastData>?,
    val humedadRelativa: List<AemetForecastData>?,
    val vientoAndRachaMax: List<AemetForecastData>?,
)
