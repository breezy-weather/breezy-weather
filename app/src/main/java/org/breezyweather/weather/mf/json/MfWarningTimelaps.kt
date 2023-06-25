package org.breezyweather.weather.mf.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MfWarningTimelaps(
    @SerialName("phenomenon_id") val phenomenonId: String,
    @SerialName("timelaps_items") val timelapsItems: List<MfWarningTimelapsItem>?
)
