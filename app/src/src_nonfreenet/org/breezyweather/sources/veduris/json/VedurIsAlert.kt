package org.breezyweather.sources.veduris.json

import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import java.util.Date

@Serializable
data class VedurIsAlert(
    val identifier: String,
    val icon: String?,
    @Serializable(DateSerializer::class) val startsAt: Date?,
    @Serializable(DateSerializer::class) val endsAt: Date?,
    val headline: String?,
    val description: String?,
    val impact: String?,
)
