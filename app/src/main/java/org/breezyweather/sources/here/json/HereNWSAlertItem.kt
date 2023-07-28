package org.breezyweather.sources.here.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HereNWSAlertItem(
    val counties: List<HereNWSAlertRegion>?,
    val zones: List<HereNWSAlertRegion>?,
    val provinces: List<HereNWSAlertRegion>?,
    val type: Int?,
    val description: String?,
    val severity: Int?,
    val message: String?,
    @SerialName("name") val locationName: String?,
    @SerialName("validFromTimeLocal") val start: String?,
    @SerialName("validUntilTimeLocal") val end: String?
)
