package org.breezyweather.daily.adapter.holder

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.unit.SpeedUnit
import org.breezyweather.daily.adapter.DailyWeatherAdapter
import org.breezyweather.daily.adapter.model.DailyWind
import org.breezyweather.settings.SettingsManager

class WindHolder(parent: ViewGroup) : DailyWeatherAdapter.ViewHolder(
    LayoutInflater.from(parent.context)
        .inflate(R.layout.item_weather_daily_wind, parent, false)
) {
    private val mIcon: AppCompatImageView = itemView.findViewById(R.id.item_weather_daily_wind_arrow)
    private val mDirectionText: TextView = itemView.findViewById(R.id.item_weather_daily_wind_directionValue)
    private val mSpeed: LinearLayout = itemView.findViewById(R.id.item_weather_daily_wind_speed)
    private val mSpeedText: TextView = itemView.findViewById(R.id.item_weather_daily_wind_speedValue)
    private val mStrengthText: TextView = itemView.findViewById(R.id.item_weather_daily_wind_strengthValue)
    private val mSpeedUnit: SpeedUnit = SettingsManager.getInstance(parent.context).speedUnit

    @SuppressLint("SetTextI18n", "RestrictedApi")
    override fun onBindView(model: DailyWeatherAdapter.ViewModel, position: Int) {
        val wind = (model as DailyWind).wind
        if (wind.direction != null || wind.speed != null) {
            val talkBackBuilder = StringBuilder(
                itemView.context.getString(R.string.wind)
            )
            mIcon.supportImageTintList = ColorStateList.valueOf(wind.getWindColor(itemView.context))
            if (wind.degree?.degree != null) {
                mIcon.rotation = wind.degree.degree + 180
            }
            talkBackBuilder.append(", ").append(wind.direction)
            if (wind.degree!!.isNoDirection || wind.degree.degree!! % 45 == 0f) {
                mDirectionText.text = wind.direction
            } else {
                mDirectionText.text = (wind.direction
                        + " (" + (wind.degree.degree % 360).toInt() + "°)")
            }
            if (wind.speed != null && wind.speed > 0) {
                talkBackBuilder.append(", ")
                    .append(mSpeedUnit.getValueText(mSpeedText.context, wind.speed))
                mSpeed.visibility = View.VISIBLE
                mSpeedText.text = mSpeedUnit.getValueText(mSpeedText.context, wind.speed)
            } else {
                mSpeed.visibility = View.GONE
            }
            talkBackBuilder.append(", ").append(wind.level)
            mStrengthText.text = wind.level
            itemView.contentDescription = talkBackBuilder.toString()
        } else {
            // TODO: Hide
        }
    }
}
