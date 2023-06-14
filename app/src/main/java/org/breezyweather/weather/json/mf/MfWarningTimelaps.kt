package org.breezyweather.weather.json.mf

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MfWarningTimelaps(
    @SerialName("phenomenon_id") val phenomenonId: String,
    @SerialName("timelaps_items") val timelapsItems: List<MfWarningTimelapsItem>?
)
