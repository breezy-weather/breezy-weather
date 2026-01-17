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

package org.breezyweather.sources.nominatim.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Maybe try to extract things like ISO3166-2-lvl4, ISO3166-2-lvl6, etc
 */
@Serializable
data class NominatimAddress(
    val village: String?, // District
    val town: String?, // City
    val municipality: String?, // Admin 3
    val county: String?, // Admin 2
    val state: String?, // Admin 1
    val country: String?,
    @SerialName("country_code") val countryCode: String?,
    @SerialName("ISO3166-2-lvl3") val isoLvl3: String?,
    @SerialName("ISO3166-2-lvl4") val isoLvl4: String?,
    @SerialName("ISO3166-2-lvl5") val isoLvl5: String?,
    @SerialName("ISO3166-2-lvl6") val isoLvl6: String?,
    @SerialName("ISO3166-2-lvl8") val isoLvl8: String?,
    @SerialName("ISO3166-2-lvl15") val isoLvl15: String?,
)
