package org.breezyweather.main.adapters.trend.daily

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.unit.PrecipitationUnit
import org.breezyweather.common.basic.models.weather.Precipitation
import org.breezyweather.common.ui.widgets.trend.TrendRecyclerView
import org.breezyweather.common.ui.widgets.trend.chart.DoubleHistogramView
import org.breezyweather.main.utils.MainThemeColorProvider
import org.breezyweather.settings.SettingsManager
import org.breezyweather.theme.resource.ResourceHelper
import org.breezyweather.theme.resource.providers.ResourceProvider

/**
 * Daily precipitation adapter.
 */
class DailyPrecipitationAdapter(
    activity: GeoActivity,
    location: Location,
    provider: ResourceProvider,
    unit: PrecipitationUnit
) : AbsDailyTrendAdapter(activity, location) {
    private val mResourceProvider: ResourceProvider = provider
    private val mPrecipitationUnit: PrecipitationUnit = unit
    private var mHighestPrecipitation: Float = 0f

    inner class ViewHolder(itemView: View) : AbsDailyTrendAdapter.ViewHolder(itemView) {
        private val mDoubleHistogramView: DoubleHistogramView

        init {
            mDoubleHistogramView = DoubleHistogramView(itemView.context)
            dailyItem.chartItemView = mDoubleHistogramView
        }

        @SuppressLint("SetTextI18n, InflateParams")
        fun onBindView(activity: GeoActivity, location: Location, position: Int) {
            val talkBackBuilder = StringBuilder(activity.getString(R.string.tag_precipitation))
            super.onBindView(activity, location, talkBackBuilder, position)
            val weather = location.weather!!
            val daily = weather.dailyForecast[position]
            val daytimePrecipitation = daily.day?.precipitation?.total ?: 0f
            val nighttimePrecipitation = daily.night?.precipitation?.total ?: 0f
            if (daytimePrecipitation > 0f || nighttimePrecipitation > 0f) {
                talkBackBuilder.append(", ")
                    .append(activity.getString(R.string.daytime))
                    .append(" : ")
                    .append(if (daytimePrecipitation > 0f) activity.getString(R.string.precipitation_none) else mPrecipitationUnit.getValueVoice(activity, daytimePrecipitation))
                talkBackBuilder.append(", ")
                    .append(activity.getString(R.string.nighttime))
                    .append(" : ")
                    .append(if (nighttimePrecipitation > 0f) activity.getString(R.string.precipitation_none) else mPrecipitationUnit.getValueVoice(activity, nighttimePrecipitation))
            } else {
                talkBackBuilder.append(", ")
                    .append(activity.getString(R.string.precipitation_none))
            }
            daily.day?.weatherCode?.let {
                dailyItem.setDayIconDrawable(ResourceHelper.getWeatherIcon(mResourceProvider, it, true))
            }
            mDoubleHistogramView.setData(
                daily.day?.precipitation?.total,
                daily.night?.precipitation?.total,
                mPrecipitationUnit.getValueTextWithoutUnit(daytimePrecipitation),
                mPrecipitationUnit.getValueTextWithoutUnit(nighttimePrecipitation),
                mHighestPrecipitation
            )
            mDoubleHistogramView.setLineColors(
                daily.day?.precipitation?.getPrecipitationColor(activity) ?: Color.TRANSPARENT,
                daily.night?.precipitation?.getPrecipitationColor(activity) ?: Color.TRANSPARENT,
                MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOutline)
            )
            mDoubleHistogramView.setTextColors(
                MainThemeColorProvider.getColor(location, R.attr.colorBodyText)
            )
            mDoubleHistogramView.setHistogramAlphas(1f, 0.5f)
            daily.night?.weatherCode?.let {
                dailyItem.setNightIconDrawable(ResourceHelper.getWeatherIcon(mResourceProvider, it, false))
            }
            dailyItem.contentDescription = talkBackBuilder.toString()
        }
    }

    init {
        val daytimeWithPrecipitation = location.weather!!.dailyForecast.filter { it.day?.precipitation?.total != null }
        if (daytimeWithPrecipitation.isNotEmpty()) {
            mHighestPrecipitation = daytimeWithPrecipitation.maxOf { it.day!!.precipitation!!.total!! }
        }
        val nighttimeWithPrecipitation = location.weather.dailyForecast.filter { it.night?.precipitation?.total != null }
        if (nighttimeWithPrecipitation.isNotEmpty()) {
            mHighestPrecipitation = maxOf(mHighestPrecipitation, nighttimeWithPrecipitation.maxOf { it.night!!.precipitation!!.total!! })
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

    override fun isValid(location: Location) = mHighestPrecipitation > 0

    override fun getDisplayName(context: Context) = context.getString(R.string.tag_precipitation)

    override fun bindBackgroundForHost(host: TrendRecyclerView) {
        val unit = SettingsManager.getInstance(activity).precipitationUnit
        val keyLineList: MutableList<TrendRecyclerView.KeyLine> = ArrayList()
        keyLineList.add(
            TrendRecyclerView.KeyLine(
                Precipitation.PRECIPITATION_LIGHT,
                unit.getValueTextWithoutUnit(Precipitation.PRECIPITATION_LIGHT),
                activity.getString(R.string.precipitation_intensity_light),
                TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
            )
        )
        keyLineList.add(
            TrendRecyclerView.KeyLine(
                Precipitation.PRECIPITATION_HEAVY,
                unit.getValueTextWithoutUnit(Precipitation.PRECIPITATION_HEAVY),
                activity.getString(R.string.precipitation_intensity_heavy),
                TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
            )
        )
        keyLineList.add(
            TrendRecyclerView.KeyLine(
                -Precipitation.PRECIPITATION_LIGHT,
                unit.getValueTextWithoutUnit(Precipitation.PRECIPITATION_LIGHT),
                activity.getString(R.string.precipitation_intensity_light),
                TrendRecyclerView.KeyLine.ContentPosition.BELOW_LINE
            )
        )
        keyLineList.add(
            TrendRecyclerView.KeyLine(
                -Precipitation.PRECIPITATION_HEAVY,
                unit.getValueTextWithoutUnit(Precipitation.PRECIPITATION_HEAVY),
                activity.getString(R.string.precipitation_intensity_heavy),
                TrendRecyclerView.KeyLine.ContentPosition.BELOW_LINE
            )
        )
        host.setData(keyLineList, mHighestPrecipitation, -mHighestPrecipitation)
    }
}