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
import org.breezyweather.common.serializer.DateSerializer
import org.breezyweather.sources.imgw.ImgwDailyDateSerializer
import java.util.Date

@Serializable
data class ImgwDayNightForecastEntry(
    @Serializable(ImgwDailyDateSerializer::class) @SerialName("Date") val date: Date,
    val isDay: Boolean,
    @SerialName("Icon") val icon: String,
    @SerialName("Cloud_Min") val minCloudCover: Int,
    @SerialName("Cloud_Ave") val avgCloudCover: Int,
    @SerialName("Cloud_Max") val maxCloudCover: Int,
    @SerialName("Temp_Min") val minTemperature: Double,
    @SerialName("Temp_Max") val maxTemperature: Double,
    @SerialName("Prec_Sum") val sumPrecipitation: Double,
    @SerialName("Prec_Max") val maxPrecipitation: Double,
    @SerialName("Rain_Sum") val sumRain: Double,
    @SerialName("Rain_Max") val maxRain: Double,
    @SerialName("Snow_Sum") val sumSnow: Double,
    @SerialName("Snow_Max") val maxSnow: Double,
    @SerialName("Wind_Speed_Max") val maxWindSpeed: Double,
)
