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

package org.breezyweather.sources.metoffice.json

import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import java.util.Date

@Serializable
data class MetOfficeHourly(
    @Serializable(with = DateSerializer::class)
    val time: Date,
    val screenTemperature: Double?,
    val maxScreenAirTemp: Double?,
    val minScreenAirTemp: Double?,
    val screenDewPointTemperature: Double?,
    val feelsLikeTemperature: Double?,
    val windSpeed10m: Double?,
    val windDirectionFrom10m: Int?,
    val windGustSpeed10m: Double?,
    val max10mWindGust: Double?,
    val visibility: Int?,
    val screenRelativeHumidity: Double?,
    val mslp: Int?,
    val uvIndex: Int?,
    val significantWeatherCode: Int?,
    val precipitationRate: Double?,
    val totalPrecipAmount: Double?,
    val totalSnowAmount: Double?,
    val probOfPrecipitation: Int?,
)
