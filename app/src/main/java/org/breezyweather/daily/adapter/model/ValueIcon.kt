package org.breezyweather.daily.adapter.model

import androidx.annotation.DrawableRes
import org.breezyweather.daily.adapter.DailyWeatherAdapter

class ValueIcon(
    val title: String,
    val value: String,
    @DrawableRes val icon: Int,
) : DailyWeatherAdapter.ViewModel {
    override val code = 9

    companion object {
        fun isCode(code: Int) = (code == 9)
    }
}
