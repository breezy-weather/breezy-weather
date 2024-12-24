package org.breezyweather.sources.ekuk.json

import kotlinx.serialization.Serializable

@Serializable
data class EkukObservationsResult(
    val measured: String?,
    val value: String?,
    val indicator: Int?,
)
