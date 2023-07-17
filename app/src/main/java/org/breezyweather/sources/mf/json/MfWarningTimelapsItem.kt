package org.breezyweather.sources.mf.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import java.util.Date

@Serializable
data class MfWarningTimelapsItem(
    @Serializable(DateSerializer::class) @SerialName("begin_time") val beginTime: Date,
    @Serializable(DateSerializer::class) @SerialName("end_time") val endTime: Date?,
    @SerialName("color_id") val colorId: Int
)
