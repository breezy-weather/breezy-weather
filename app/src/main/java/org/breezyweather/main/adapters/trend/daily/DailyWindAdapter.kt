package org.breezyweather.main.adapters.trend.daily

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.unit.SpeedUnit
import org.breezyweather.common.basic.models.weather.Wind
import org.breezyweather.common.ui.images.RotateDrawable
import org.breezyweather.common.ui.widgets.trend.TrendRecyclerView
import org.breezyweather.common.ui.widgets.trend.TrendRecyclerView.KeyLine
import org.breezyweather.common.ui.widgets.trend.chart.DoubleHistogramView
import org.breezyweather.main.utils.MainThemeColorProvider
import org.breezyweather.settings.SettingsManager

/**
 * Daily wind adapter.
 */
class DailyWindAdapter(activity: GeoActivity, location: Location, unit: SpeedUnit) : AbsDailyTrendAdapter(
    activity, location
) {
    private val mSpeedUnit: SpeedUnit = unit
    private var mHighestWindSpeed: Float = 0f

    inner class ViewHolder(itemView: View) : AbsDailyTrendAdapter.ViewHolder(itemView) {
        private val mDoubleHistogramView = DoubleHistogramView(itemView.context)

        init {
            dailyItem.chartItemView = mDoubleHistogramView
        }

        @SuppressLint("SetTextI18n, InflateParams")
        fun onBindView(activity: GeoActivity, location: Location, position: Int) {
            val talkBackBuilder = StringBuilder(activity.getString(R.string.tag_wind))
            super.onBindView(activity, location, talkBackBuilder, position)
            val daily = location.weather!!.dailyForecast[position]
            daily.day?.wind?.let { wind ->
                talkBackBuilder
                    .append(", ").append(activity.getString(R.string.daytime))
                    .append(" : ").append(wind.getWindDescription(activity, mSpeedUnit))
                val dayIcon = if (wind.isValidSpeed) RotateDrawable(
                    ContextCompat.getDrawable(
                        activity,
                        R.drawable.ic_navigation
                    )
                ) else RotateDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_circle_medium))
                wind.degree?.degree?.let {
                    dayIcon.rotate(it + 180)
                }
                dayIcon.colorFilter = PorterDuffColorFilter(wind.getWindColor(activity), PorterDuff.Mode.SRC_ATOP)
                dailyItem.setDayIconDrawable(dayIcon)
            }
            daily.night?.wind?.let {
                talkBackBuilder
                    .append(", ").append(activity.getString(R.string.nighttime))
                    .append(" : ").append(it.getWindDescription(activity, mSpeedUnit))
            }
            if (daily.day?.wind != null && daily.night?.wind != null) {
                mDoubleHistogramView.setData(
                    daily.day.wind.speed,
                    daily.night.wind.speed,
                    mSpeedUnit.getValueTextWithoutUnit(daily.day.wind.speed ?: 0f),
                    mSpeedUnit.getValueTextWithoutUnit(daily.night.wind.speed ?: 0f),
                    mHighestWindSpeed
                )
                mDoubleHistogramView.setLineColors(
                    daily.day.wind.getWindColor(activity),
                    daily.night.wind.getWindColor(activity),
                    MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOutline)
                )
                mDoubleHistogramView.setTextColors(MainThemeColorProvider.getColor(location, R.attr.colorBodyText))
                mDoubleHistogramView.setHistogramAlphas(1f, 0.5f)
            }
            daily.night?.wind?.let { wind ->
                val nightIcon = if (wind.isValidSpeed) RotateDrawable(
                    ContextCompat.getDrawable(
                        activity,
                        R.drawable.ic_navigation
                    )
                ) else RotateDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_circle_medium))
                wind.degree?.degree?.let {
                    nightIcon.rotate(it + 180)
                }
                nightIcon.colorFilter = PorterDuffColorFilter(wind.getWindColor(activity), PorterDuff.Mode.SRC_ATOP)
                dailyItem.setNightIconDrawable(nightIcon)
            }
            dailyItem.contentDescription = talkBackBuilder.toString()
        }
    }

    init {
        val daytimeWithWindSpeed = location.weather!!.dailyForecast.filter { it.day?.wind?.speed != null }
        if (daytimeWithWindSpeed.isNotEmpty()) {
            mHighestWindSpeed = daytimeWithWindSpeed.maxOf { it.day!!.wind!!.speed!! }
        }
        val nighttimeWithWindSpeed = location.weather.dailyForecast.filter { it.night?.wind?.speed != null }
        if (nighttimeWithWindSpeed.isNotEmpty()) {
            mHighestWindSpeed = maxOf(mHighestWindSpeed, nighttimeWithWindSpeed.maxOf { it.night!!.wind!!.speed!! })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trend_daily, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: AbsDailyTrendAdapter.ViewHolder, position: Int) {
        (holder as ViewHolder).onBindView(activity, location, position)
    }

    override fun getItemCount() = location.weather!!.dailyForecast.size

    override fun isValid(location: Location) = mHighestWindSpeed > 0

    override fun getDisplayName(context: Context) = context.getString(R.string.tag_wind)

    override fun bindBackgroundForHost(host: TrendRecyclerView) {
        val unit = SettingsManager.getInstance(activity).speedUnit
        val keyLineList: MutableList<KeyLine> = ArrayList()
        keyLineList.add(
            KeyLine(
                Wind.WIND_SPEED_3,
                unit.getValueTextWithoutUnit(Wind.WIND_SPEED_3),
                activity.getString(R.string.wind_level_3),
                KeyLine.ContentPosition.ABOVE_LINE
            )
        )
        keyLineList.add(
            KeyLine(
                Wind.WIND_SPEED_7,
                unit.getValueTextWithoutUnit(Wind.WIND_SPEED_7),
                activity.getString(R.string.wind_level_7),
                KeyLine.ContentPosition.ABOVE_LINE
            )
        )
        keyLineList.add(
            KeyLine(
                -Wind.WIND_SPEED_3,
                unit.getValueTextWithoutUnit(Wind.WIND_SPEED_3),
                activity.getString(R.string.wind_level_3),
                KeyLine.ContentPosition.BELOW_LINE
            )
        )
        keyLineList.add(
            KeyLine(
                -Wind.WIND_SPEED_7,
                unit.getValueTextWithoutUnit(Wind.WIND_SPEED_7),
                activity.getString(R.string.wind_level_7),
                KeyLine.ContentPosition.BELOW_LINE
            )
        )
        host.setData(keyLineList, mHighestWindSpeed, -mHighestWindSpeed)
    }
}