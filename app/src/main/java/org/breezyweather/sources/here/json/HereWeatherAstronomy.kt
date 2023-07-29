package org.breezyweather.sources.here.json

import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import java.util.Date

/**
 * Don’t include sunrise/sunset, moonrise/moonset, they only have hours which is known to cause
 * issues when on different days (midnight solar)
 */
@Serializable
data class HereWeatherAstronomy(
    @Serializable(DateSerializer::class) val time: Date?,
    val moonPhase: Float?
)
