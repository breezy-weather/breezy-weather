package org.breezyweather.sources.here.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import java.util.Date

@Serializable
data class HereNWSAlertItem(
    val type: Int?,
    val description: String?,
    val severity: Int?,
    val message: String?,
    @SerialName("name") val locationName: String?,
    @Serializable(DateSerializer::class) val validFromTimeLocal: Date?,
    @Serializable(DateSerializer::class) val validUntilTimeLocal: Date?
)
