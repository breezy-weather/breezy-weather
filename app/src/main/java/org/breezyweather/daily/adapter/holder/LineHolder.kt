package org.breezyweather.daily.adapter.holder

import android.view.LayoutInflater
import android.view.ViewGroup
import org.breezyweather.R
import org.breezyweather.daily.adapter.DailyWeatherAdapter

class LineHolder(parent: ViewGroup) : DailyWeatherAdapter.ViewHolder(
    LayoutInflater.from(parent.context)
        .inflate(R.layout.item_weather_daily_line, parent, false)
) {
    override fun onBindView(model: DailyWeatherAdapter.ViewModel, position: Int) {
        // do nothing.
    }
}
