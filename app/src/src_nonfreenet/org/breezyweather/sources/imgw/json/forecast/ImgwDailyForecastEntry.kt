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

package org.breezyweather.sources.imgw.json.forecast

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.breezyweather.sources.imgw.ImgwDailyDateSerializer
import java.util.Date

@Serializable
data class ImgwDailyForecastEntry(
    @SerialName("Temp_Min") val minTemperature: Double,
    @SerialName("Temp_Max") val maxTemperature: Double,
    @SerialName("Prec_Max") val maxPrecipitation: Double,
    @SerialName("Cloud_Min") val minCloudCover: Int,
    @SerialName("Cloud_Ave") val avgCloudCover: Int,
    @SerialName("Cloud_Max") val maxCloudCover: Int,
    @SerialName("Wind_Speed_Max") val maxWindSpeed: Double,
    @SerialName("Icon") val icon: String,
    @Serializable(ImgwDailyDateSerializer::class) @SerialName("Date") val date: Date,
)
