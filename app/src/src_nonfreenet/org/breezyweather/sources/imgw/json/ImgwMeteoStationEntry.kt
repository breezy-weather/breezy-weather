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

package org.breezyweather.sources.imgw.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ImgwMeteoStationEntry(
    @SerialName("kod_stacji") val stationId: String,
    @SerialName("nazwa_stacji") val stationName: String,
    @SerialName("lon") val longitude: Double?,
    @SerialName("lat") val latitude: Double?,
    @SerialName("temperatura_gruntu") val surfaceTemperature: Double?,
    @SerialName("temperatura_powietrza") val airTemperature: Double?,
    @SerialName("wiatr_kierunek") val windDirection: Double?,
    @SerialName("wiatr_srednia_predkosc") val avgWindSpeed: Double?,
    @SerialName("wiatr_predkosc_maksymalna") val maxWindSpeed: Double?,
    @SerialName("wilgotnosc_wzgledna") val relativeHumidity: Int?,
    @SerialName("wiatr_poryw_10min") val windGust: Double?,
    @SerialName("opad_10min") val precipitation: Double?,
)
