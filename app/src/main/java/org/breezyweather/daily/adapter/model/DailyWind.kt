package org.breezyweather.daily.adapter.model

import org.breezyweather.common.basic.models.weather.Wind
import org.breezyweather.daily.adapter.DailyWeatherAdapter

class DailyWind(
    val wind: Wind
) : DailyWeatherAdapter.ViewModel {
    override val code = 4

    companion object {
        fun isCode(code: Int) = (code == 4)
    }
}
