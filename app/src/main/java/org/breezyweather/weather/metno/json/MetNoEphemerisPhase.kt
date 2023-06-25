package org.breezyweather.weather.metno.json

import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import java.util.*

@Serializable
data class MetNoEphemerisPhase(
    @Serializable(DateSerializer::class) val time: Date?
)
