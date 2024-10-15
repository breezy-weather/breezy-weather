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

package org.breezyweather.sources.common.xml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue
import org.breezyweather.common.serializer.DateSerializer
import java.util.Date

/**
 * Common Alerting Protocol (CAP) v1.2
 * https://docs.oasis-open.org/emergency/cap/v1.2/CAP-v1.2-os.html
 *
 * Standard format for alerts that can be re-used in any source
 */
@Serializable
@XmlSerialName("alert", "urn:oasis:names:tc:emergency:cap:1.2", "cap")
data class CapAlert(
    val identifier: Identifier? = null,
    val sent: Sent? = null,
    val info: List<Info>? = null
) {
    @Serializable
    @XmlSerialName("identifier", "", "cap")
    data class Identifier(
        @XmlValue(true) val value: String? = null
    )

    @Serializable
    @XmlSerialName("sent", "", "cap")
    data class Sent(
        @XmlValue(true) @Serializable(DateSerializer::class) val value: Date? = null
    )

    @Serializable
    @XmlSerialName("info", "urn:oasis:names:tc:emergency:cap:1.2", "cap")
    data class Info(
        val language: Language? = null,
        val event: Event? = null,
        val severity: Severity? = null,
        val effective: Effective? = null,
        val onset: Onset? = null,
        val expires: Expires? = null,
        val senderName: SenderName? = null,
        val headline: Headline? = null,
        val description: Description? = null,
        val instruction: Instruction? = null
    ) {
        @Serializable
        @XmlSerialName("language", "", "cap")
        data class Language(
            @XmlValue(true) val value: String? = null
        )

        @Serializable
        @XmlSerialName("event", "", "cap")
        data class Event(
            @XmlValue(true) val value: String? = null
        )

        @Serializable
        @XmlSerialName("severity", "", "cap")
        data class Severity(
            @XmlValue(true) val value: String? = null
        )

        @Serializable
        @XmlSerialName("effective", "", "cap")
        data class Effective(
            @XmlValue(true) @Serializable(DateSerializer::class) val value: Date? = null
        )

        @Serializable
        @XmlSerialName("onset", "", "cap")
        data class Onset(
            @XmlValue(true) @Serializable(DateSerializer::class) val value: Date? = null
        )

        @Serializable
        @XmlSerialName("expires", "", "cap")
        data class Expires(
            @XmlValue(true) @Serializable(DateSerializer::class) val value: Date? = null
        )

        @Serializable
        @XmlSerialName("senderName", "", "cap")
        data class SenderName(
            @XmlValue(true) val value: String? = null
        )

        @Serializable
        @XmlSerialName("headline", "", "cap")
        data class Headline(
            @XmlValue(true) val value: String? = null
        )

        @Serializable
        @XmlSerialName("description", "", "cap")
        data class Description(
            @XmlValue(true) val value: String? = null
        )

        @Serializable
        @XmlSerialName("instruction", "", "cap")
        data class Instruction(
            @XmlValue(true) val value: String? = null
        )
    }
}
