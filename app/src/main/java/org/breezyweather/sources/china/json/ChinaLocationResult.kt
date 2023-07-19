package org.breezyweather.sources.china.json

import kotlinx.serialization.Serializable

@Serializable
data class ChinaLocationResult(
    val affiliation: String?,
    val key: String?,
    val latitude: String?,
    val locationKey: String?,
    val longitude: String?,
    val name: String?,
    val status: Int?,
    val timeZoneShift: Int?
)
