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

package org.breezyweather.sources.metoffice.json

import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateUtcSerializer
import java.util.Date

@Serializable
data class MetOfficeGeometry(
    val type: String?,
    val coordinates: List<Double>?,
)

@Serializable
data class MetOfficeLocation(
    val name: String?,
)

@Serializable
data class MetOfficeFeatureProperties<T>(
    val location: MetOfficeLocation?,
    val requestPointDistance: Double?,
    @Serializable(with = DateUtcSerializer::class)
    val modelRunDate: Date?,
    val timeSeries: List<T>,
)

@Serializable
data class MetOfficeFeature<T>(
    val geometry: MetOfficeGeometry?,
    val properties: MetOfficeFeatureProperties<T>,
)

@Serializable
data class MetOfficeForecast<T>(
    val features: List<MetOfficeFeature<T>>,
)
