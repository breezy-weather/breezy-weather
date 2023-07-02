package org.breezyweather.main.adapters.trend.hourly

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.unit.PrecipitationUnit
import org.breezyweather.common.basic.models.weather.Precipitation
import org.breezyweather.common.ui.widgets.trend.TrendRecyclerView
import org.breezyweather.common.ui.widgets.trend.TrendRecyclerView.KeyLine
import org.breezyweather.common.ui.widgets.trend.chart.PolylineAndHistogramView
import org.breezyweather.main.utils.MainThemeColorProvider
import org.breezyweather.settings.SettingsManager
import org.breezyweather.theme.ThemeManager
import org.breezyweather.theme.resource.ResourceHelper
import org.breezyweather.theme.resource.providers.ResourceProvider
import org.breezyweather.theme.weatherView.WeatherViewController.getWeatherKind

/**
 * Hourly precipitation adapter.
 */
class HourlyPrecipitationAdapter(
    activity: GeoActivity,
    location: Location,
    provider: ResourceProvider,
    unit: PrecipitationUnit
) : AbsHourlyTrendAdapter(activity, location) {
    private val mResourceProvider: ResourceProvider = provider
    private val mPrecipitationUnit: PrecipitationUnit = unit
    private var mHighestPrecipitation: Float = 0f

    inner class ViewHolder(itemView: View) : AbsHourlyTrendAdapter.ViewHolder(itemView) {
        private val mPolylineAndHistogramView = PolylineAndHistogramView(itemView.context)

        init {
            hourlyItem.chartItemView = mPolylineAndHistogramView
        }

        fun onBindView(activity: GeoActivity, location: Location, position: Int) {
            val talkBackBuilder = StringBuilder(activity.getString(R.string.tag_precipitation))
            super.onBindView(activity, location, talkBackBuilder, position)
            val weather = location.weather!!
            val hourly = weather.hourlyForecast[position]
            hourlyItem.setIconDrawable(
                ResourceHelper.getWeatherIcon(mResourceProvider, hourly.weatherCode, hourly.isDaylight)
            )
            hourly.precipitation?.let {
                val precipitation = hourly.precipitation.total ?: 0f
                if (precipitation > 0f) {
                    talkBackBuilder.append(", ")
                        .append(mPrecipitationUnit.getValueVoice(activity, precipitation))
                } else {
                    talkBackBuilder.append(", ")
                        .append(activity.getString(R.string.precipitation_none))
                }
                mPolylineAndHistogramView.setData(
                    null, null,
                    null, null,
                    null, null,
                    precipitation,
                    mPrecipitationUnit.getValueTextWithoutUnit(precipitation),
                    mHighestPrecipitation,
                    0f
                )
                mPolylineAndHistogramView.setLineColors(
                    hourly.precipitation.getPrecipitationColor(activity),
                    hourly.precipitation.getPrecipitationColor(activity),
                    MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOutline)
                )
            }
            val themeColors = ThemeManager
                .getInstance(itemView.context)
                .weatherThemeDelegate
                .getThemeColors(
                    itemView.context,
                    getWeatherKind(location.weather),
                    location.isDaylight
                )
            val lightTheme = MainThemeColorProvider.isLightTheme(itemView.context, location)
            mPolylineAndHistogramView.setShadowColors(
                themeColors[if (lightTheme) 1 else 2],
                themeColors[2],
                lightTheme
            )
            mPolylineAndHistogramView.setTextColors(
                MainThemeColorProvider.getColor(location, R.attr.colorTitleText),
                MainThemeColorProvider.getColor(location, R.attr.colorBodyText),
                MainThemeColorProvider.getColor(location, R.attr.colorTitleText)
            )
            mPolylineAndHistogramView.setHistogramAlpha(if (lightTheme) 1f else 0.5f)
            hourlyItem.contentDescription = talkBackBuilder.toString()
        }
    }

    init {
        val hourlyWithPrecipitation = location.weather!!.hourlyForecast.filter { it.precipitation?.total != null }
        if (hourlyWithPrecipitation.isNotEmpty()) {
            mHighestPrecipitation = hourlyWithPrecipitation.maxOf { it.precipitation!!.total!! }
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
        assert(location.weather != null)
        return location.weather!!.hourlyForecast.size
    }

    override fun isValid(location: Location): Boolean {
        return mHighestPrecipitation > 0
    }

    override fun getDisplayName(context: Context): String {
        return context.getString(R.string.tag_precipitation)
    }

    override fun bindBackgroundForHost(host: TrendRecyclerView) {
        val unit = SettingsManager.getInstance(activity).precipitationUnit
        val keyLineList: MutableList<KeyLine> = ArrayList()
        keyLineList.add(
            KeyLine(
                Precipitation.PRECIPITATION_LIGHT,
                activity.getString(R.string.precipitation_intensity_light),
                unit.getValueTextWithoutUnit(Precipitation.PRECIPITATION_LIGHT),
                KeyLine.ContentPosition.ABOVE_LINE
            )
        )
        keyLineList.add(
            KeyLine(
                Precipitation.PRECIPITATION_HEAVY,
                activity.getString(R.string.precipitation_intensity_heavy),
                unit.getValueTextWithoutUnit(Precipitation.PRECIPITATION_HEAVY),
                KeyLine.ContentPosition.ABOVE_LINE
            )
        )
        host.setData(keyLineList, mHighestPrecipitation, 0f)
    }
}