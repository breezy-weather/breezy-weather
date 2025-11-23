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

package org.breezyweather.sources.lvgmc.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LvgmcForecastResult(
    @SerialName("punkts") val point: String?,
    @SerialName("nosaukums") val name: String?,
    @SerialName("novads") val municipality: String?,
    @SerialName("lon") val longitude: String?,
    @SerialName("lat") val latitude: String?,
    @SerialName("laiks") val time: String?,
    @SerialName("temperatura") val temperature: String?,
    @SerialName("veja_atrums") val windSpeed: String?,
    @SerialName("veja_virziens") val windDirection: String?,
    @SerialName("brazmas") val windGusts: String?,
    @SerialName("nokrisni_1h") val precipitation1h: String?,
    @SerialName("nokrisni_12h") val precipitation12h: String?,
    @SerialName("relativais_mitrums") val relativeHumidity: String?,
    @SerialName("laika_apstaklu_ikona") val icon: String?,
    @SerialName("spiediens") val pressure: String?,
    @SerialName("sajutu_temperatura") val apparentTemperature: String?,
    @SerialName("sniegs") val snow: String?,
    @SerialName("makoni") val cloudCover: String?,
    @SerialName("nokrisnu_varbutiba") val precipitationProbability: String?,
    @SerialName("uvi_indekss") val uvIndex: String?,
    @SerialName("perkons") val thunderstormProbability: String?,
)
