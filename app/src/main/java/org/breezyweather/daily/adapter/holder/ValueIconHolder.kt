package org.breezyweather.daily.adapter.holder

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import org.breezyweather.R
import org.breezyweather.daily.adapter.DailyWeatherAdapter
import org.breezyweather.daily.adapter.model.ValueIcon

class ValueIconHolder(parent: ViewGroup) : DailyWeatherAdapter.ViewHolder(
    LayoutInflater.from(parent.context)
        .inflate(R.layout.item_weather_daily_valueicon, parent, false)
) {
    private val mIcon: ImageView = itemView.findViewById(R.id.item_weather_daily_valueicon_icon)
    private val mText: TextView = itemView.findViewById(R.id.item_weather_daily_valueicon_text)
    private val mValue: TextView = itemView.findViewById(R.id.item_weather_daily_valueicon_value)

    @SuppressLint("SetTextI18n")
    override fun onBindView(model: DailyWeatherAdapter.ViewModel, position: Int) {
        mText.text = (model as ValueIcon).title
        mValue.text = model.value
        mIcon.setImageResource(model.icon)
    }
}