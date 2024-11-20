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

package org.breezyweather.sources.ipma.json

import kotlinx.serialization.Serializable

@Serializable
data class IpmaForecastResult(
    val dataPrev: String,
    val idPeriodo: Int,
    val tMed: String?,
    val tMin: String?,
    val tMax: String?,
    val iUv: String?,
    val idTipoTempo: Int?,
    val probabilidadePrecipita: String?,
    val hR: String?,
    val utci: String?,
    val ffVento: String?,
    val ddVento: String?,
)
