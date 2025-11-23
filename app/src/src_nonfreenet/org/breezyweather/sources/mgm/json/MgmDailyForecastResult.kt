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

package org.breezyweather.sources.mgm.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MgmDailyForecastResult(
    @SerialName("enDusukGun1") val minTempDay1: Double?,
    @SerialName("enDusukGun2") val minTempDay2: Double?,
    @SerialName("enDusukGun3") val minTempDay3: Double?,
    @SerialName("enDusukGun4") val minTempDay4: Double?,
    @SerialName("enDusukGun5") val minTempDay5: Double?,
    @SerialName("enYuksekGun1") val maxTempDay1: Double?,
    @SerialName("enYuksekGun2") val maxTempDay2: Double?,
    @SerialName("enYuksekGun3") val maxTempDay3: Double?,
    @SerialName("enYuksekGun4") val maxTempDay4: Double?,
    @SerialName("enYuksekGun5") val maxTempDay5: Double?,
    @SerialName("hadiseGun1") val conditionDay1: String?,
    @SerialName("hadiseGun2") val conditionDay2: String?,
    @SerialName("hadiseGun3") val conditionDay3: String?,
    @SerialName("hadiseGun4") val conditionDay4: String?,
    @SerialName("hadiseGun5") val conditionDay5: String?,
    @SerialName("ruzgarHizGun1") val windSpeedDay1: Double?,
    @SerialName("ruzgarHizGun2") val windSpeedDay2: Double?,
    @SerialName("ruzgarHizGun3") val windSpeedDay3: Double?,
    @SerialName("ruzgarHizGun4") val windSpeedDay4: Double?,
    @SerialName("ruzgarHizGun5") val windSpeedDay5: Double?,
    @SerialName("ruzgarYonGun1") val windDirectionDay1: Double?,
    @SerialName("ruzgarYonGun2") val windDirectionDay2: Double?,
    @SerialName("ruzgarYonGun3") val windDirectionDay3: Double?,
    @SerialName("ruzgarYonGun4") val windDirectionDay4: Double?,
    @SerialName("ruzgarYonGun5") val windDirectionDay5: Double?,
    // Do not use @Serializable(DateSerializer::class) for "tarihGunX".
    // The timestamps are in actually Europe/Istanbul, not Etc/UTC.
    // The 'Z' at the end of the timestamps are misused.
    @SerialName("tarihGun1") val dateDay1: String,
    @SerialName("tarihGun2") val dateDay2: String,
    @SerialName("tarihGun3") val dateDay3: String,
    @SerialName("tarihGun4") val dateDay4: String,
    @SerialName("tarihGun5") val dateDay5: String,
)
