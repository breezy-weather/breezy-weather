package org.breezyweather.daily.adapter.holder

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import org.breezyweather.R
import org.breezyweather.daily.adapter.DailyWeatherAdapter
import org.breezyweather.daily.adapter.model.Value

class ValueHolder(parent: ViewGroup) : DailyWeatherAdapter.ViewHolder(
    LayoutInflater.from(parent.context)
        .inflate(R.layout.item_weather_daily_value, parent, false)
) {
    private val mTitle: TextView = itemView.findViewById(R.id.item_weather_daily_value_title)
    private val mValue: TextView = itemView.findViewById(R.id.item_weather_daily_value_value)

    @SuppressLint("RtlHardcoded")
    override fun onBindView(model: DailyWeatherAdapter.ViewModel, position: Int) {
        val v = model as Value
        mTitle.text = v.title
        mValue.text = v.value
        itemView.contentDescription = mTitle.text.toString() + ", " + mValue.text
    }
}
