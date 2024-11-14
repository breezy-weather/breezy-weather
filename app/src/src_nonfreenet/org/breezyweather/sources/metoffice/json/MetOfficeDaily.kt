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
data class MetOfficeDaily(
    @Serializable(with = DateSerializer::class)
    val time: Date,
    val maxUvIndex: Int?,
    val daySignificantWeatherCode: Int?,
    val nightSignificantWeatherCode: Int?,
    val dayMaxScreenTemperature: Double?,
    val nightMinScreenTemperature: Double?,
    val dayUpperBoundMaxTemp: Double?,
    val nightUpperBoundMinTemp: Double?,
    val dayLowerBoundMaxTemp: Double?,
    val nightLowerBoundMinTemp: Double?,
    val dayMaxFeelsLikeTemp: Double?,
    val nightMinFeelsLikeTemp: Double?,
    val dayUpperBoundMaxFeelsLikeTemp: Double?,
    val nightUpperBoundMinFeelsLikeTemp: Double?,
    val dayLowerBoundMaxFeelsLikeTemp: Double?,
    val nightLowerBoundMinFeelsLikeTemp: Double?,
    val dayProbabilityOfPrecipitation: Int?,
    val nightProbabilityOfPrecipitation: Int?,
    val dayProbabilityOfSnow: Int?,
    val nightProbabilityOfSnow: Int?,
    val dayProbabilityOfHeavySnow: Int?,
    val nightProbabilityOfHeavySnow: Int?,
    val dayProbabilityOfRain: Int?,
    val nightProbabilityOfRain: Int?,
    val dayProbabilityOfHeavyRain: Int?,
    val nightProbabilityOfHeavyRain: Int?,
    val dayProbabilityOfHail: Int?,
    val nightProbabilityOfHail: Int?,
    val dayProbabilityOfSferics: Int?,
    val nightProbabilityOfSferics: Int?,
)
