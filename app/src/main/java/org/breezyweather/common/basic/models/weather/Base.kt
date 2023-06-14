package org.breezyweather.common.basic.models.weather

import java.io.Serializable
import java.util.Date

/**
 * Base.
 */
class Base(
    val cityId: String,
    val publishDate: Date = Date(),
    val updateDate: Date = Date()
) : Serializable
