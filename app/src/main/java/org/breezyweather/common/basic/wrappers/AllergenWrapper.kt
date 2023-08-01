package org.breezyweather.common.basic.wrappers

import org.breezyweather.common.basic.models.weather.Allergen
import java.util.Date

data class AllergenWrapper(
    val current: Allergen? = null, // TODO: Not supported yet
    val dailyForecast: Map<Date, Allergen>? = null,
    val hourlyForecast: Map<Date, Allergen>? = null
)