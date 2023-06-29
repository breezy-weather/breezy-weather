package org.breezyweather.daily.adapter.model

import org.breezyweather.common.basic.models.weather.Astro
import org.breezyweather.common.basic.models.weather.MoonPhase
import org.breezyweather.daily.adapter.DailyWeatherAdapter
import java.util.*

class DailyAstro(
    val timeZone: TimeZone,
    val sun: Astro?,
    val moon: Astro?,
    val moonPhase: MoonPhase?
) : DailyWeatherAdapter.ViewModel {
    override val code = 7

    companion object {
        fun isCode(code: Int) = (code == 7)
    }
}
