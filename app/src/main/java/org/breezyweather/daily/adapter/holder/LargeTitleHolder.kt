package org.breezyweather.daily.adapter.holder

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import org.breezyweather.R
import org.breezyweather.daily.adapter.DailyWeatherAdapter
import org.breezyweather.daily.adapter.model.LargeTitle

class LargeTitleHolder(parent: ViewGroup) : DailyWeatherAdapter.ViewHolder(
    LayoutInflater.from(parent.context)
        .inflate(R.layout.item_weather_daily_title_large, parent, false)
) {
    override fun onBindView(model: DailyWeatherAdapter.ViewModel, position: Int) {
        val title = model as LargeTitle
        (itemView as TextView).text = title.title
    }
}
