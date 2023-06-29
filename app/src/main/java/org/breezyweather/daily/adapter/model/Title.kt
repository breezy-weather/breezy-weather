package org.breezyweather.daily.adapter.model

import androidx.annotation.DrawableRes
import org.breezyweather.daily.adapter.DailyWeatherAdapter

class Title(
    @field:DrawableRes val resId: Int? = null,
    val title: String
) : DailyWeatherAdapter.ViewModel {
    override val code = 2

    companion object {
        fun isCode(code: Int) = (code == 2)
    }
}
