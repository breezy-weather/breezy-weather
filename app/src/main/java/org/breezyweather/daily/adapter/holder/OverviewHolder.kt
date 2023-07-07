package org.breezyweather.daily.adapter.holder

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.ui.widgets.AnimatableIconView
import org.breezyweather.daily.adapter.DailyWeatherAdapter
import org.breezyweather.daily.adapter.model.Overview
import org.breezyweather.settings.SettingsManager
import org.breezyweather.theme.resource.ResourcesProviderFactory
import org.breezyweather.theme.resource.providers.ResourceProvider

class OverviewHolder(parent: ViewGroup) : DailyWeatherAdapter.ViewHolder(
    LayoutInflater.from(parent.context)
        .inflate(R.layout.item_weather_daily_overview, parent, false)
) {
    private val mIcon: AnimatableIconView = itemView.findViewById(R.id.item_weather_daily_overview_icon)
    private val mTitle: TextView = itemView.findViewById(R.id.item_weather_daily_overview_text)
    private val mProvider: ResourceProvider = ResourcesProviderFactory.newInstance
    private val mTemperatureUnit: TemperatureUnit = SettingsManager.getInstance(parent.context).temperatureUnit

    init {
        itemView.setOnClickListener { mIcon.startAnimators() }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindView(model: DailyWeatherAdapter.ViewModel, position: Int) {
        val overview = model as Overview
        if (overview.halfDay.weatherCode != null) {
            mIcon.setAnimatableIcon(
                mProvider.getWeatherIcons(overview.halfDay.weatherCode, overview.isDaytime),
                mProvider.getWeatherAnimators(overview.halfDay.weatherCode, overview.isDaytime)
            )
        }
        val builder = StringBuilder()
        if (!overview.halfDay.weatherText .isNullOrEmpty()) {
            builder.append(overview.halfDay.weatherText)
        }
        if (overview.halfDay.temperature != null
            && !overview.halfDay.temperature.getTemperature(mTitle.context, mTemperatureUnit).isNullOrEmpty()
        ) {
            if (builder.toString().isNotEmpty()) {
                builder.append(", ")
            }
            builder.append(overview.halfDay.temperature.getTemperature(mTitle.context, mTemperatureUnit))
        }
        if (builder.toString().isNotEmpty()) {
            mTitle.text = builder.toString()
        }
    }
}
