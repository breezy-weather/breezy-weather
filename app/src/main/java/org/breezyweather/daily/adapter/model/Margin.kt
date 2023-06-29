package org.breezyweather.daily.adapter.model

import org.breezyweather.daily.adapter.DailyWeatherAdapter

class Margin : DailyWeatherAdapter.ViewModel {
    override val code = -2

    companion object {
        fun isCode(code: Int) = (code == -2)
    }
}
