package org.breezyweather.common.basic.models.weather

import java.io.Serializable
import java.util.Date

/**
 * History.
 */
class History(
    val date: Date,
    val daytimeTemperature: Int? = null,
    val nighttimeTemperature: Int? = null
) : Serializable
