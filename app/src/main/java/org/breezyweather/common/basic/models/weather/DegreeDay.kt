package org.breezyweather.common.basic.models.weather

import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import java.io.Serializable

/**
 * Degree Day
 * default unit : [TemperatureUnit.C]
 */
class DegreeDay(
    val heating: Float? = null,
    val cooling: Float? = null,
) : Serializable