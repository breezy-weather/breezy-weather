package org.breezyweather.daily.adapter.model

import org.breezyweather.daily.adapter.DailyWeatherAdapter

class Line : DailyWeatherAdapter.ViewModel {
    override val code = -1

    companion object {
        fun isCode(code: Int) = (code == -1)
    }
}
