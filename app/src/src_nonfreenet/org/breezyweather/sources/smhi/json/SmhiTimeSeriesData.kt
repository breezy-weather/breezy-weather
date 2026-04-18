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

package org.breezyweather.sources.smhi.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SmhiTimeSeriesData(
    @SerialName("air_temperature") val airTemperature: Double? = null,
    @SerialName("wind_from_direction") val windFromDirection: Double? = null,
    @SerialName("wind_speed") val windSpeed: Double? = null,
    @SerialName("wind_speed_of_gust") val windSpeedOfGust: Double? = null,
    @SerialName("relative_humidity") val relativeHumidity: Double? = null,
    @SerialName("air_pressure_at_mean_sea_level") val airPressureAtMeanSeaLevel: Double? = null,
    @SerialName("visibility_in_air") val visibilityInAir: Double? = null,
    @SerialName("thunderstorm_probability") val thunderstormProbability: Double? = null,
    @SerialName("precipitation_amount_mean") val precipitationAmountMean: Double? = null,
    @SerialName("symbol_code") val symbolCode: Double? = null,
)
