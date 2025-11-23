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

package org.breezyweather.sources.metno.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MetNoForecastDataDetails(
    @SerialName("air_pressure_at_sea_level") val airPressureAtSeaLevel: Double?,
    @SerialName("air_temperature") val airTemperature: Double?,
    @SerialName("dew_point_temperature") val dewPointTemperature: Double?,
    @SerialName("precipitation_rate") val precipitationRate: Double?,
    @SerialName("precipitation_amount") val precipitationAmount: Double?,
    @SerialName("probability_of_precipitation") val probabilityOfPrecipitation: Double?,
    @SerialName("probability_of_thunder") val probabilityOfThunder: Double?,
    @SerialName("relative_humidity") val relativeHumidity: Double?,
    @SerialName("ultraviolet_index_clear_sky") val ultravioletIndexClearSky: Double?,
    @SerialName("wind_from_direction") val windFromDirection: Double?,
    @SerialName("wind_speed") val windSpeed: Double?,
    @SerialName("cloud_area_fraction") val cloudAreaFraction: Double?,
)
