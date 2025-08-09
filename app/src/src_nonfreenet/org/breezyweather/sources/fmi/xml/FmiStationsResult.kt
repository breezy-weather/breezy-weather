package org.breezyweather.sources.fmi.xml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
@XmlSerialName("FeatureCollection", NS_WFS, "fmi")
data class FmiStationsResult(
    val members: List<Member>? = null,
) {
    @Serializable
    @XmlSerialName("member", NS_WFS, "fmi")
    data class Member(
        val environmentalMonitoringFacility: EnvironmentalMonitoringFacility?,
    ) {
        @Serializable
        @XmlSerialName("EnvironmentalMonitoringFacility", NS_EF, "fmi")
        data class EnvironmentalMonitoringFacility(
            val identifier: Identifier?,
            val name: Name?,
            val representativePoint: RepresentativePoint?,
            val mobile: Mobile?,
            val belongsTo: List<BelongsTo>?,
        ) {
            @Serializable
            @XmlSerialName("identifier", NS_GML, "fmi")
            data class Identifier(
                @XmlValue(true) val value: String?,
            )

            @Serializable
            @XmlSerialName("name", NS_EF, "fmi")
            data class Name(
                @XmlValue(true) val value: String?,
            )

            @Serializable
            @XmlSerialName("representativePoint", NS_EF, "fmi")
            data class RepresentativePoint(
                val point: Point?,
            ) {
                @Serializable
                @XmlSerialName("Point", NS_GML, "fmi")
                data class Point(
                    val pos: Pos?,
                ) {
                    @Serializable
                    @XmlSerialName("pos", NS_GML, "fmi")
                    data class Pos(
                        @XmlValue(true) val value: String?,
                    )
                }
            }

            @Serializable
            @XmlSerialName("mobile", NS_EF, "fmi")
            data class Mobile(
                @XmlValue(true) val value: Boolean?,
            )

            @Serializable
            @XmlSerialName("belongsTo", NS_EF, "fmi")
            data class BelongsTo(
                @XmlSerialName("title", NS_XLINK, "fmi") val title: String?,
            )
        }
    }
}

private const val NS_WFS = "http://www.opengis.net/wfs/2.0"
private const val NS_EF = "http://inspire.ec.europa.eu/schemas/ef/4.0"
private const val NS_GML = "http://www.opengis.net/gml/3.2"
private const val NS_XLINK = "http://www.w3.org/1999/xlink"
