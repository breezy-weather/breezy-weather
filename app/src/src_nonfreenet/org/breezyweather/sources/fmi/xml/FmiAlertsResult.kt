package org.breezyweather.sources.fmi.xml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
@XmlSerialName("rss", "", "fmi")
data class FmiAlertsResult(
    val channel: Channel? = null,
) {
    @Serializable
    @XmlSerialName("channel", "", "fmi")
    data class Channel(
        val items: List<Item>?,
    ) {
        @Serializable
        @XmlSerialName("item", "", "fmi")
        data class Item(
            val title: Title?,
            val link: Link?,
        ) {
            @Serializable
            @XmlSerialName("title", "", "fmi")
            data class Title(
                @XmlValue(true) val value: String?,
            )

            @Serializable
            @XmlSerialName("link", "", "fmi")
            data class Link(
                @XmlValue(true) val value: String?,
            )
        }
    }
}
