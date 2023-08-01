package org.breezyweather.sources.mf.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import java.util.Date

@Serializable
data class MfRainResult(
    @SerialName("update_time") @Serializable(DateSerializer::class) val updateTime: Date? = null,
    val properties: MfRainProperties? = null
)