package org.breezyweather.main.adapters.trend.daily

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.unit.SpeedUnit
import org.breezyweather.common.basic.models.weather.Wind
import org.breezyweather.common.ui.images.RotateDrawable
import org.breezyweather.common.ui.widgets.trend.TrendRecyclerView
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

            if (daily.day?.wind != null && daily.day.wind.isValid) {
                talkBackBuilder
                    .append(", ").append(activity.getString(R.string.daytime))
                    .append(" : ").append(daily.day.wind.getDescription(activity, mSpeedUnit))
            }
            val dayWindColor = daily.day?.wind?.getColor(activity) ?: Color.TRANSPARENT
            val dayIcon = if (daily.day?.wind?.degree == -1f) {
                AppCompatResources.getDrawable(activity, R.drawable.ic_replay)
            } else if (daily.day?.wind?.degree != null) {
                RotateDrawable(
                    AppCompatResources.getDrawable(activity, R.drawable.ic_navigation)
                ).apply {
                    rotate(daily.day.wind.degree + 180)
                }
            } else null
            dayIcon?.colorFilter = PorterDuffColorFilter(dayWindColor, PorterDuff.Mode.SRC_ATOP)
            dailyItem.setDayIconDrawable(dayIcon, missingIconVisibility = View.INVISIBLE)

            val nightWindColor = daily.night?.wind?.getColor(activity) ?: Color.TRANSPARENT

            mDoubleHistogramView.setData(
                daily.day?.wind?.speed ?: 0f,
                daily.night?.wind?.speed ?: 0f,
                daily.day?.wind?.speed?.let { mSpeedUnit.getValueTextWithoutUnit(it) },
                daily.night?.wind?.speed?.let { mSpeedUnit.getValueTextWithoutUnit(it) },
                mHighestWindSpeed
            )
            mDoubleHistogramView.setLineColors(
                dayWindColor,
                nightWindColor,
                MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOutline)
            )
            mDoubleHistogramView.setTextColors(MainThemeColorProvider.getColor(location, R.attr.colorBodyText))
            mDoubleHistogramView.setHistogramAlphas(1f, 0.5f)

            if (daily.night?.wind != null && daily.night.wind.isValid) {
                talkBackBuilder
                    .append(", ").append(activity.getString(R.string.nighttime))
                    .append(" : ").append(daily.night.wind.getDescription(activity, mSpeedUnit))
            }
            val nightIcon = if (daily.night?.wind?.degree == -1f) {
                AppCompatResources.getDrawable(activity, R.drawable.ic_replay)
            } else if (daily.night?.wind?.degree != null) {
                RotateDrawable(
                    AppCompatResources.getDrawable(activity, R.drawable.ic_navigation)
                ).apply {
                    rotate(daily.night.wind.degree + 180)
                }
            } else null
            nightIcon?.colorFilter = PorterDuffColorFilter(nightWindColor, PorterDuff.Mode.SRC_ATOP)
            dailyItem.setNightIconDrawable(nightIcon, missingIconVisibility = View.INVISIBLE)

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
        val keyLineList: MutableList<TrendRecyclerView.KeyLine> = ArrayList()
        keyLineList.add(
            TrendRecyclerView.KeyLine(
                Wind.WIND_SPEED_3,
                unit.getValueTextWithoutUnit(Wind.WIND_SPEED_3),
                activity.getString(R.string.wind_strength_3),
                TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
            )
        )
        keyLineList.add(
            TrendRecyclerView.KeyLine(
                Wind.WIND_SPEED_7,
                unit.getValueTextWithoutUnit(Wind.WIND_SPEED_7),
                activity.getString(R.string.wind_strength_7),
                TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
            )
        )
        keyLineList.add(
            TrendRecyclerView.KeyLine(
                -Wind.WIND_SPEED_3,
                unit.getValueTextWithoutUnit(Wind.WIND_SPEED_3),
                activity.getString(R.string.wind_strength_3),
                TrendRecyclerView.KeyLine.ContentPosition.BELOW_LINE
            )
        )
        keyLineList.add(
            TrendRecyclerView.KeyLine(
                -Wind.WIND_SPEED_7,
                unit.getValueTextWithoutUnit(Wind.WIND_SPEED_7),
                activity.getString(R.string.wind_strength_7),
                TrendRecyclerView.KeyLine.ContentPosition.BELOW_LINE
            )
        )
        host.setData(keyLineList, mHighestWindSpeed, -mHighestWindSpeed)
    }
}