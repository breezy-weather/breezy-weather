package org.breezyweather.daily.adapter.model

import org.breezyweather.common.basic.models.weather.UV
import org.breezyweather.daily.adapter.DailyWeatherAdapter

class DailyUV(
    val uv: UV
) : DailyWeatherAdapter.ViewModel {
    override val code = 8

    companion object {
        fun isCode(code: Int) = (code == 8)
    }
}
