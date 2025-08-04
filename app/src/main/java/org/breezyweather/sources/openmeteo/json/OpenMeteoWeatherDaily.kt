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

package org.breezyweather.sources.openmeteo.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenMeteoWeatherDaily(
    val time: LongArray,
    @SerialName("temperature_2m_max") val temperatureMax: Array<Double?>?,
    @SerialName("temperature_2m_min") val temperatureMin: Array<Double?>?,
    @SerialName("apparent_temperature_max") val apparentTemperatureMax: Array<Double?>?,
    @SerialName("apparent_temperature_min") val apparentTemperatureMin: Array<Double?>?,
    @SerialName("sunshine_duration") val sunshineDuration: Array<Double?>?,
    @SerialName("uv_index_max") val uvIndexMax: Array<Double?>?,
    @SerialName("relative_humidity_2m_mean") val relativeHumidityMean: Array<Int?>?,
    @SerialName("relative_humidity_2m_max") val relativeHumidityMax: Array<Int?>?,
    @SerialName("relative_humidity_2m_min") val relativeHumidityMin: Array<Int?>?,
    @SerialName("dew_point_2m_mean") val dewPointMean: Array<Double?>?,
    @SerialName("dew_point_2m_max") val dewPointMax: Array<Double?>?,
    @SerialName("dew_point_2m_min") val dewPointMin: Array<Double?>?,
    @SerialName("pressure_msl_mean") val pressureMslMean: Array<Double?>?,
    @SerialName("pressure_msl_max") val pressureMslMax: Array<Double?>?,
    @SerialName("pressure_msl_min") val pressureMslMin: Array<Double?>?,
    @SerialName("cloud_cover_mean") val cloudCoverMean: Array<Int?>?,
    @SerialName("cloud_cover_max") val cloudCoverMax: Array<Int?>?,
    @SerialName("cloud_cover_min") val cloudCoverMin: Array<Int?>?,
    @SerialName("visibility_mean") val visibilityMean: Array<Double?>?,
    @SerialName("visibility_max") val visibilityMax: Array<Double?>?,
    @SerialName("visibility_min") val visibilityMin: Array<Double?>?,
)
