package org.breezyweather.sources.metno.json

import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import java.util.Date

@Serializable
data class MetNoEphemerisProperty(
    @Serializable(DateSerializer::class) val time: Date?,
)
