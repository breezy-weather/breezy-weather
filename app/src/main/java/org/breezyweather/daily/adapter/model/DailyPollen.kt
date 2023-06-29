package org.breezyweather.daily.adapter.model

import org.breezyweather.common.basic.models.weather.Pollen
import org.breezyweather.daily.adapter.DailyWeatherAdapter

class DailyPollen(
    val pollen: Pollen
) : DailyWeatherAdapter.ViewModel {
    override val code = 6

    companion object {
        fun isCode(code: Int) = (code == 6)
    }
}
