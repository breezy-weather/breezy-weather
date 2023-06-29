package org.breezyweather.daily.adapter.holder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import org.breezyweather.R
import org.breezyweather.daily.adapter.DailyWeatherAdapter
import org.breezyweather.daily.adapter.model.Title

class TitleHolder(parent: ViewGroup) : DailyWeatherAdapter.ViewHolder(
    LayoutInflater.from(parent.context)
        .inflate(R.layout.item_weather_daily_title, parent, false)
) {
    private val mIcon: ImageView = itemView.findViewById(R.id.item_weather_daily_title_icon)
    private val mTitle: TextView = itemView.findViewById(R.id.item_weather_daily_title_title)

    override fun onBindView(model: DailyWeatherAdapter.ViewModel, position: Int) {
        val t = model as Title
        if (t.resId != null) {
            mIcon.visibility = View.VISIBLE
            mIcon.setImageResource(t.resId)
        } else {
            mIcon.visibility = View.GONE
        }
        mTitle.text = t.title
    }
}
