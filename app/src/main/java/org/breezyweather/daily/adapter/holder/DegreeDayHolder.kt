package org.breezyweather.daily.adapter.holder

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import org.breezyweather.R
import org.breezyweather.daily.adapter.DailyWeatherAdapter
import org.breezyweather.daily.adapter.model.DailyDegreeDay
import org.breezyweather.settings.SettingsManager

class DegreeDayHolder(parent: ViewGroup) : DailyWeatherAdapter.ViewHolder(
    LayoutInflater.from(parent.context)
        .inflate(R.layout.item_weather_daily_degreeday, parent, false)
) {
    private val mHeating: LinearLayout = itemView.findViewById(R.id.item_weather_daily_degreeday_heating)
    private val mHeatingText: TextView = itemView.findViewById(R.id.item_weather_daily_degreeday_heatingText)
    private val mCooling: LinearLayout = itemView.findViewById(R.id.item_weather_daily_degreeday_cooling)
    private val mCoolingText: TextView = itemView.findViewById(R.id.item_weather_daily_degreeday_coolingText)

    @SuppressLint("SetTextI18n")
    override fun onBindView(model: DailyWeatherAdapter.ViewModel, position: Int) {
        val context = itemView.context
        val talkBackBuilder = StringBuilder(context.getString(R.string.temperature_degree_day))
        val temperatureUnit = SettingsManager.getInstance((model as DailyDegreeDay).context).temperatureUnit
        if (model.degreeDay.heating != null) {
            val heatingText = temperatureUnit.getValueText(model.context, model.degreeDay.heating)
            talkBackBuilder
                .append(", ")
                .append(context.getString(R.string.temperature_degree_day_heating))
                .append(heatingText)
            mHeating.visibility = View.VISIBLE
            mHeatingText.text = temperatureUnit.getValueText(model.context, model.degreeDay.heating)
        } else {
            mHeating.visibility = View.GONE
        }
        if (model.degreeDay.cooling != null) {
            val coolingText = temperatureUnit.getValueText(model.context, model.degreeDay.cooling)
            talkBackBuilder
                .append(", ")
                .append(context.getString(R.string.ephemeris_moonrise_at))
                .append(coolingText)
            mCooling.visibility = View.VISIBLE
            mCoolingText.text = temperatureUnit.getValueText(model.context, model.degreeDay.cooling)
        } else {
            mCooling.visibility = View.GONE
        }
        itemView.contentDescription = talkBackBuilder.toString()
    }
}