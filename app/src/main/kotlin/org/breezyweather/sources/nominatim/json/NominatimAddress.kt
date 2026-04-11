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
    // Country
    val country: String?,
    @SerialName("country_code") val countryCode: String?,

    // Admin1
    val state: String?,

    // Admin2
    val county: String?,

    // Admin3
    val municipality: String?,

    // Admin4
    val village: String?,
    val town: String?,
    val city: String?,

    // Admin5
    @SerialName("city_district") val cityDistrict: String?,
    val district: String?,
    val borough: String?,
    val suburb: String?,
    val subdivision: String?,

    val hamlet: String?,
    val croft: String?,
    @SerialName("isolated_dwelling") val isolatedDwelling: String?,

    val neighbourhood: String?,
    val allotments: String?,
    val quarter: String?,

    // ISO levels
    @SerialName("ISO3166-2-lvl3") val isoLvl3: String?,
    @SerialName("ISO3166-2-lvl4") val isoLvl4: String?,
    @SerialName("ISO3166-2-lvl5") val isoLvl5: String?,
    @SerialName("ISO3166-2-lvl6") val isoLvl6: String?,
    @SerialName("ISO3166-2-lvl8") val isoLvl8: String?,
    @SerialName("ISO3166-2-lvl15") val isoLvl15: String?,
)
