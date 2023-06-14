package org.breezyweather.main.adapters.main.holder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.ui.widgets.precipitationBar.PrecipitationBar
import org.breezyweather.main.adapters.trend.HourlyTrendAdapter
import org.breezyweather.main.utils.MainThemeColorProvider.Companion.getColor
import org.breezyweather.main.utils.MainThemeColorProvider.Companion.isLightTheme
import org.breezyweather.settings.SettingsManager
import org.breezyweather.theme.ThemeManager

private fun needToShowMinutelyForecast(minutelyList: List<org.breezyweather.common.basic.models.weather.Minutely>) =
    minutelyList.firstOrNull { (it.precipitationIntensity ?: 0.0) > 0.0 } != null

class HourlyViewHolder(
    parent: ViewGroup
) : AbstractMainCardViewHolder(
        LayoutInflater
            .from(parent.context)
            .inflate(R.layout.container_main_hourly_trend_card, parent, false)
) {
    private val title: TextView = itemView.findViewById(R.id.container_main_hourly_trend_card_title)
    private val subtitle: TextView = itemView.findViewById(R.id.container_main_hourly_trend_card_subtitle)
    private val tagView: RecyclerView = itemView.findViewById(R.id.container_main_hourly_trend_card_tagView)
    private val trendRecyclerView: org.breezyweather.common.ui.widgets.trend.TrendRecyclerView = itemView.findViewById(R.id.container_main_hourly_trend_card_trendRecyclerView)
    private val scrollBar: org.breezyweather.main.widgets.TrendRecyclerViewScrollBar =
        org.breezyweather.main.widgets.TrendRecyclerViewScrollBar()
    private val minutelyContainer: LinearLayout = itemView.findViewById(R.id.container_main_hourly_trend_card_minutely)
    private val minutelyTitle: TextView = itemView.findViewById(R.id.container_main_hourly_trend_card_minutelyTitle)
    private val precipitationBar: PrecipitationBar = itemView.findViewById(R.id.container_main_hourly_trend_card_minutelyBar)
    private val minutelyStartText: TextView = itemView.findViewById(R.id.container_main_hourly_trend_card_minutelyStartText)
    private val minutelyCenterText: TextView = itemView.findViewById(R.id.container_main_hourly_trend_card_minutelyCenterText)
    private val minutelyEndText: TextView = itemView.findViewById(R.id.container_main_hourly_trend_card_minutelyEndText)
    private val minutelyStartLine: View = itemView.findViewById(R.id.container_main_hourly_trend_card_minutelyStartLine)
    private val minutelyEndLine: View = itemView.findViewById(R.id.container_main_hourly_trend_card_minutelyEndLine)

    init {
        trendRecyclerView.setHasFixedSize(true)
        trendRecyclerView.addItemDecoration(scrollBar)

        minutelyContainer.setOnClickListener { /* do nothing. */ }
    }

    override fun onBindView(
        activity: GeoActivity,
        location: Location,
        provider: org.breezyweather.theme.resource.providers.ResourceProvider,
        listAnimationEnabled: Boolean,
        itemAnimationEnabled: Boolean,
        firstCard: Boolean
    ) {
        super.onBindView(
            activity,
            location,
            provider,
            listAnimationEnabled,
            itemAnimationEnabled,
            firstCard
        )

        val weather = location.weather ?: return
        val colors = ThemeManager
            .getInstance(context)
            .weatherThemeDelegate
            .getThemeColors(
                context,
                org.breezyweather.theme.weatherView.WeatherViewController.getWeatherKind(weather),
                location.isDaylight
            )

        title.setTextColor(colors[0])

        if (weather.current?.hourlyForecast.isNullOrEmpty()) {
            subtitle.visibility = View.GONE
        } else {
            subtitle.visibility = View.VISIBLE
            subtitle.text = weather.current?.hourlyForecast
        }

        val trendAdapter = HourlyTrendAdapter(activity, trendRecyclerView).apply {
            bindData(location)
        }
        val tagList = trendAdapter.adapters.map {
            org.breezyweather.common.ui.adapters.TagAdapter.Tag {
                it.getDisplayName(activity)
            }
        }

        if (tagList.size < 2) {
            tagView.visibility = View.GONE
        } else {
            val decorCount = tagView.itemDecorationCount
            for (i in 0 until decorCount) {
                tagView.removeItemDecorationAt(0)
            }
            tagView.addItemDecoration(
                org.breezyweather.common.ui.decotarions.GridMarginsDecoration(
                    context.resources.getDimension(R.dimen.little_margin),
                    context.resources.getDimension(R.dimen.normal_margin),
                    tagView
                )
            )
            tagView.layoutManager =
                org.breezyweather.main.layouts.TrendHorizontalLinearLayoutManager(context)
            tagView.adapter = org.breezyweather.common.ui.adapters.TagAdapter(
                tagList,
                getColor(location, com.google.android.material.R.attr.colorOnPrimary),
                getColor(location, com.google.android.material.R.attr.colorOnSurface),
                getColor(location, androidx.appcompat.R.attr.colorPrimary),
                org.breezyweather.common.utils.DisplayUtils.getWidgetSurfaceColor(
                    org.breezyweather.common.utils.DisplayUtils.DEFAULT_CARD_LIST_ITEM_ELEVATION_DP,
                    getColor(location, androidx.appcompat.R.attr.colorPrimary),
                    getColor(location, com.google.android.material.R.attr.colorSurface)
                ),
                { _, _, newPosition: Int ->
                    trendAdapter.selectedIndex = newPosition
                    return@TagAdapter false
                },
                0
            )
        }

        trendRecyclerView.layoutManager =
            org.breezyweather.main.layouts.TrendHorizontalLinearLayoutManager(
                context,
                if (org.breezyweather.common.utils.DisplayUtils.isLandscape(context)) 7 else 5
            )
        trendRecyclerView.setLineColor(getColor(location, com.google.android.material.R.attr.colorOutline))
        trendRecyclerView.adapter = trendAdapter
        trendRecyclerView.setKeyLineVisibility(
            SettingsManager.getInstance(context).isTrendHorizontalLinesEnabled
        )

        scrollBar.resetColor(location)

        val minutelyList = weather.minutelyForecast
        if (minutelyList.size >= 3 && needToShowMinutelyForecast(minutelyList)) {
            minutelyContainer.visibility = View.VISIBLE
            precipitationBar.precipitationIntensities = minutelyList.map {
                it.precipitationIntensity ?: 0.0
            }.toTypedArray()
            precipitationBar.indicatorGenerator = object : PrecipitationBar.IndicatorGenerator {
                override fun getIndicatorContent(precipitation: Double) =
                    SettingsManager
                        .getInstance(activity)
                        .precipitationIntensityUnit
                        .getValueText(activity, precipitation.toFloat())
            }

            val size = minutelyList.size
            minutelyStartText.text = org.breezyweather.common.utils.DisplayUtils.getTime(context, minutelyList[0].date, location.timeZone)
            minutelyCenterText.text = org.breezyweather.common.utils.DisplayUtils.getTime(context, minutelyList[(size - 1) / 2].date, location.timeZone)
            minutelyEndText.text = org.breezyweather.common.utils.DisplayUtils.getTime(context, minutelyList[size - 1].date, location.timeZone)
            minutelyContainer.contentDescription =
                activity.getString(R.string.content_des_minutely_precipitation)
                    .replace("$1", org.breezyweather.common.utils.DisplayUtils.getTime(context, minutelyList[0].date, location.timeZone))
                    .replace("$2", org.breezyweather.common.utils.DisplayUtils.getTime(context, minutelyList[size - 1].date, location.timeZone))
        } else {
            minutelyContainer.visibility = View.GONE
        }

        minutelyTitle.setTextColor(colors[0])

        precipitationBar.precipitationColor = ThemeManager
            .getInstance(context)
            .weatherThemeDelegate
            .getThemeColors(
                context,
                org.breezyweather.theme.weatherView.WeatherViewController.getWeatherKind(weather),
                location.isDaylight
            )[0]
        precipitationBar.subLineColor = getColor(location, com.google.android.material.R.attr.colorOutline)
        precipitationBar.highlightColor = getColor(location, androidx.appcompat.R.attr.colorPrimary)
        precipitationBar.setShadowColors(colors[0], colors[1], isLightTheme(itemView.context, location))

        minutelyStartText.setTextColor(getColor(location, R.attr.colorBodyText))
        minutelyCenterText.setTextColor(getColor(location, R.attr.colorBodyText))
        minutelyEndText.setTextColor(getColor(location, R.attr.colorBodyText))

        minutelyStartLine.setBackgroundColor(getColor(location, com.google.android.material.R.attr.colorOutline))
        minutelyEndLine.setBackgroundColor(getColor(location, com.google.android.material.R.attr.colorOutline))
    }
}