package org.breezyweather.daily.adapter.model

import android.content.Context
import org.breezyweather.common.basic.models.weather.AirQuality
import org.breezyweather.common.basic.models.weather.DegreeDay
import org.breezyweather.daily.adapter.DailyWeatherAdapter

class DailyDegreeDay(
    val context: Context,
    val degreeDay: DegreeDay
) : DailyWeatherAdapter.ViewModel {
    override val code = 9

    companion object {
        fun isCode(code: Int) = (code == 9)
    }
}
