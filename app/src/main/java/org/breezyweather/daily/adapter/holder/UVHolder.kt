package org.breezyweather.daily.adapter.holder

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.widget.ImageViewCompat
import org.breezyweather.R
import org.breezyweather.daily.adapter.DailyWeatherAdapter
import org.breezyweather.daily.adapter.model.DailyUV

class UVHolder(parent: ViewGroup) : DailyWeatherAdapter.ViewHolder(
    LayoutInflater.from(parent.context)
        .inflate(R.layout.item_weather_daily_uv, parent, false)
) {
    private val mIcon: AppCompatImageView = itemView.findViewById(R.id.item_weather_daily_uv_icon)
    private val mTitle: TextView = itemView.findViewById(R.id.item_weather_daily_uv_title)

    override fun onBindView(model: DailyWeatherAdapter.ViewModel, position: Int) {
        val context = itemView.context
        val uv = (model as DailyUV).uv
        ImageViewCompat.setImageTintList(mIcon, ColorStateList.valueOf(uv.getUVColor(context)))
        mTitle.text = uv.uVDescription
        itemView.contentDescription = context.getString(R.string.uv_index) + ", " + mTitle.text
    }
}