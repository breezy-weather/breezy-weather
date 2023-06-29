package org.breezyweather.daily.adapter.model

import org.breezyweather.common.basic.models.weather.AirQuality
import org.breezyweather.daily.adapter.DailyWeatherAdapter

class DailyAirQuality(
    val airQuality: AirQuality
) : DailyWeatherAdapter.ViewModel {
    override val code = 5

    companion object {
        fun isCode(code: Int) = (code == 5)
    }
}
