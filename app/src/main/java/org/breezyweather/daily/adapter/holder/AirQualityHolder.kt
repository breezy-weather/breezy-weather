package org.breezyweather.daily.adapter.holder

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import org.breezyweather.R
import org.breezyweather.common.ui.widgets.RoundProgress
import org.breezyweather.daily.adapter.DailyWeatherAdapter
import org.breezyweather.daily.adapter.model.DailyAirQuality

class AirQualityHolder(parent: ViewGroup) : DailyWeatherAdapter.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.item_weather_daily_air, parent, false)
) {
    private val mProgress: RoundProgress = itemView.findViewById(R.id.item_weather_daily_air_progress)
    private val mContent: TextView = itemView.findViewById(R.id.item_weather_daily_air_content)

    @SuppressLint("SetTextI18n")
    override fun onBindView(model: DailyWeatherAdapter.ViewModel, position: Int) {
        val airQuality = (model as DailyAirQuality).airQuality
        val aqi = airQuality.getIndex()
        val color = airQuality.getColor(itemView.context)
        mProgress.apply {
            max = 400f
            if (aqi != null) {
                progress = aqi.toFloat()
            }
            setProgressColor(color)
            setProgressBackgroundColor(ColorUtils.setAlphaComponent(color, (255 * 0.1).toInt()))
        }
        mContent.text = aqi.toString() + " / " + airQuality.getName(itemView.context)
    }
}