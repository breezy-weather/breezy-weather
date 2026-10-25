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

package org.breezyweather.sources.cymeteodep.xml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

enum class ObservationName(val apiName: String) {
    TEMPERATURE("Air Temperature (1.2m)"),
    WIND_SPEED("Wind Speed (2m)"),
    RELATIVE_HUMIDITY("Relative Humidity (1.2m)"),
    PRESSURE("Atmospheric Pressure (Station Level)"),
}

@Serializable
data class CyMeteoDepartmentObservation(
    @XmlElement(true) @XmlSerialName("observation_name") val name: String,
    @XmlElement(true) @XmlSerialName("observation_value") val value: String,
    @XmlElement(true) @XmlSerialName("observation_unit") val unit: String,
)
