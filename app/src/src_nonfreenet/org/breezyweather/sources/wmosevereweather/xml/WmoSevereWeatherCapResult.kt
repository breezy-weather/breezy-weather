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

package org.breezyweather.sources.wmosevereweather.xml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue
import org.breezyweather.common.serializer.DateSerializer
import java.util.Date

@Serializable
@XmlSerialName("alert", "urn:oasis:names:tc:emergency:cap:1.2", "")
data class WmoSevereWeatherCapResult(
    val identifier: Identifier?,
    val sent: Sent?,
    val info: List<Info>?
) {
    @Serializable
    @XmlSerialName("identifier", "", "")
    data class Identifier(
        @XmlValue(true) val value: String?
    )

    @Serializable
    @XmlSerialName("sent", "", "")
    data class Sent(
        @XmlValue(true) @Serializable(DateSerializer::class) val value: Date?
    )

    @Serializable
    @XmlSerialName("info", "urn:oasis:names:tc:emergency:cap:1.2", "")
    data class Info(
        val language: Language?,
        val event: Event?,
        val severity: Severity?,
        val effective: Effective?,
        val onset: Onset?,
        val expires: Expires?,
        val senderName: SenderName?,
        val headline: Headline?,
        val description: Description?,
        val instruction: Instruction?
    ) {
        @Serializable
        @XmlSerialName("language", "", "")
        data class Language(
            @XmlValue(true) val value: String?
        )

        @Serializable
        @XmlSerialName("event", "", "")
        data class Event(
            @XmlValue(true) val value: String?
        )

        @Serializable
        @XmlSerialName("severity", "", "")
        data class Severity(
            @XmlValue(true) val value: String?
        )

        @Serializable
        @XmlSerialName("effective", "", "")
        data class Effective(
            @XmlValue(true) @Serializable(DateSerializer::class) val value: Date?
        )

        @Serializable
        @XmlSerialName("onset", "", "")
        data class Onset(
            @XmlValue(true) @Serializable(DateSerializer::class) val value: Date?
        )

        @Serializable
        @XmlSerialName("expires", "", "")
        data class Expires(
            @XmlValue(true) @Serializable(DateSerializer::class) val value: Date?
        )

        @Serializable
        @XmlSerialName("senderName", "", "")
        data class SenderName(
            @XmlValue(true) val value: String?
        )

        @Serializable
        @XmlSerialName("headline", "", "")
        data class Headline(
            @XmlValue(true) val value: String?
        )

        @Serializable
        @XmlSerialName("description", "", "")
        data class Description(
            @XmlValue(true) val value: String?
        )

        @Serializable
        @XmlSerialName("instruction", "", "")
        data class Instruction(
            @XmlValue(true) val value: String?
        )
    }
}
