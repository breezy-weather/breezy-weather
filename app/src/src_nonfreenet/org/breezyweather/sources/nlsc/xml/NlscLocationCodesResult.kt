package org.breezyweather.sources.nlsc.xml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

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
@Serializable
@XmlSerialName("townVillageItem", "", "nlsc")
data class NlscLocationCodesResult(
    val countyCode: CountyCode? = null,
    val countyName: CountyName? = null,
    val townshipCode: TownshipCode? = null,
    val townshipName: TownshipName? = null,
    val villageCode: VillageCode? = null,
    val villageName: VillageName? = null,
) {
    @Serializable
    @XmlSerialName("ctyCode", "", "nlsc")
    data class CountyCode(
        @XmlValue(true) val value: String,
    )

    @Serializable
    @XmlSerialName("ctyName", "", "nlsc")
    data class CountyName(
        @XmlValue(true) val value: String,
    )

    @Serializable
    @XmlSerialName("townCode", "", "nlsc")
    data class TownshipCode(
        @XmlValue(true) val value: String,
    )

    @Serializable
    @XmlSerialName("townName", "", "nlsc")
    data class TownshipName(
        @XmlValue(true) val value: String,
    )

    @Serializable
    @XmlSerialName("villageCode", "", "nlsc")
    data class VillageCode(
        @XmlValue(true) val value: String,
    )

    @Serializable
    @XmlSerialName("villageName", "", "nlsc")
    data class VillageName(
        @XmlValue(true) val value: String,
    )
}
