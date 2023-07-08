package org.breezyweather.main.adapters.trend.hourly

import android.annotation.SuppressLint
import android.content.Context
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
import org.breezyweather.common.ui.widgets.trend.chart.PolylineAndHistogramView
import org.breezyweather.main.utils.MainThemeColorProvider

/**
 * Hourly wind adapter.
 */
class HourlyWindAdapter(activity: GeoActivity, location: Location, unit: SpeedUnit) : AbsHourlyTrendAdapter(
    activity, location
) {
    private val mSpeedUnit: SpeedUnit = unit
    private var mHighestWindSpeed: Float = 0f

    inner class ViewHolder(itemView: View) : AbsHourlyTrendAdapter.ViewHolder(itemView) {
        private val mPolylineAndHistogramView = PolylineAndHistogramView(itemView.context)

        init {
            hourlyItem.chartItemView = mPolylineAndHistogramView
        }

        @SuppressLint("SetTextI18n, InflateParams")
        fun onBindView(activity: GeoActivity, location: Location, position: Int) {
            val talkBackBuilder = StringBuilder(activity.getString(R.string.tag_wind))
            super.onBindView(activity, location, talkBackBuilder, position)
            val hourly = location.weather!!.hourlyForecast[position]
            hourly.wind?.let { wind ->
                talkBackBuilder
                    .append(", ").append(activity.getString(R.string.tag_wind))
                    .append(" : ").append(wind.getWindDescription(activity, mSpeedUnit))
                val windColor = wind.getWindColor(activity)
                val dayIcon = if (wind.isValidSpeed) RotateDrawable(
                    AppCompatResources.getDrawable(
                        activity,
                        R.drawable.ic_navigation
                    )
                ) else RotateDrawable(AppCompatResources.getDrawable(activity, R.drawable.ic_circle_medium))
                wind.degree?.degree?.let {
                    dayIcon.rotate(it + 180)
                }
                dayIcon.colorFilter = PorterDuffColorFilter(windColor, PorterDuff.Mode.SRC_ATOP)
                hourlyItem.setIconDrawable(dayIcon)
                mPolylineAndHistogramView.setData(
                    null, null,
                    null, null,
                    null, null,
                    wind.speed,
                    mSpeedUnit.getValueTextWithoutUnit(wind.speed ?: 0f),
                    mHighestWindSpeed, 0f
                )
                mPolylineAndHistogramView.setLineColors(
                    windColor,
                    windColor,
                    MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOutline)
                )
            }
            mPolylineAndHistogramView.setTextColors(
                MainThemeColorProvider.getColor(location, R.attr.colorTitleText),
                MainThemeColorProvider.getColor(location, R.attr.colorBodyText),
                MainThemeColorProvider.getColor(location, R.attr.colorTitleText)
            )
            mPolylineAndHistogramView.setHistogramAlpha(1f)
            hourlyItem.contentDescription = talkBackBuilder.toString()
        }
    }

    init {
        val hourlyWithWindSpeed = location.weather!!.hourlyForecast.filter { it.wind?.speed != null }
        if (hourlyWithWindSpeed.isNotEmpty()) {
            mHighestWindSpeed = hourlyWithWindSpeed.maxOf { it.wind!!.speed!! }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trend_hourly, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: AbsHourlyTrendAdapter.ViewHolder, position: Int) {
        (holder as ViewHolder).onBindView(activity, location, position)
    }

    override fun getItemCount(): Int {
        return location.weather!!.hourlyForecast.size
    }

    override fun isValid(location: Location): Boolean {
        return mHighestWindSpeed > 0
    }

    override fun getDisplayName(context: Context): String {
        return context.getString(R.string.tag_wind)
    }

    override fun bindBackgroundForHost(host: TrendRecyclerView) {
        val keyLineList: MutableList<TrendRecyclerView.KeyLine> = ArrayList()
        keyLineList.add(
            TrendRecyclerView.KeyLine(
                Wind.WIND_SPEED_3,
                mSpeedUnit.getValueTextWithoutUnit(Wind.WIND_SPEED_3),
                activity.getString(R.string.wind_level_3),
                TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
            )
        )
        keyLineList.add(
            TrendRecyclerView.KeyLine(
                Wind.WIND_SPEED_7,
                mSpeedUnit.getValueTextWithoutUnit(Wind.WIND_SPEED_7),
                activity.getString(R.string.wind_level_7),
                TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
            )
        )
        keyLineList.add(
            TrendRecyclerView.KeyLine(
                -Wind.WIND_SPEED_3,
                mSpeedUnit.getValueTextWithoutUnit(Wind.WIND_SPEED_3),
                activity.getString(R.string.wind_level_3),
                TrendRecyclerView.KeyLine.ContentPosition.BELOW_LINE
            )
        )
        keyLineList.add(
            TrendRecyclerView.KeyLine(
                -Wind.WIND_SPEED_7,
                mSpeedUnit.getValueTextWithoutUnit(Wind.WIND_SPEED_7),
                activity.getString(R.string.wind_level_7),
                TrendRecyclerView.KeyLine.ContentPosition.BELOW_LINE
            )
        )
        host.setData(keyLineList, mHighestWindSpeed, 0f)
    }
}