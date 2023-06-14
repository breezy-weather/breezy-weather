package org.breezyweather.main.adapters.main.holder

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.basic.models.Location
import org.breezyweather.main.adapters.trend.DailyTrendAdapter
import org.breezyweather.main.utils.MainThemeColorProvider.Companion.getColor
import org.breezyweather.settings.SettingsManager
import org.breezyweather.theme.ThemeManager

class DailyViewHolder(
    parent: ViewGroup
) : AbstractMainCardViewHolder(
    LayoutInflater
        .from(parent.context)
        .inflate(R.layout.container_main_daily_trend_card, parent, false)
) {
    private val title: TextView = itemView.findViewById(R.id.container_main_daily_trend_card_title)
    private val subtitle: TextView = itemView.findViewById(R.id.container_main_daily_trend_card_subtitle)
    private val tagView: RecyclerView = itemView.findViewById(R.id.container_main_daily_trend_card_tagView)
    private val trendRecyclerView: org.breezyweather.common.ui.widgets.trend.TrendRecyclerView = itemView.findViewById(R.id.container_main_daily_trend_card_trendRecyclerView)
    private val scrollBar = org.breezyweather.main.widgets.TrendRecyclerViewScrollBar()

    init {
        trendRecyclerView.setHasFixedSize(true)
        trendRecyclerView.addItemDecoration(scrollBar)
    }

    @SuppressLint("NotifyDataSetChanged")
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

        if (weather.current?.dailyForecast.isNullOrEmpty()) {
            subtitle.visibility = View.GONE
        } else {
            subtitle.visibility = View.VISIBLE
            subtitle.text = weather.current?.dailyForecast
        }

        val trendAdapter = DailyTrendAdapter(activity, trendRecyclerView).apply {
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
            tagView.visibility = View.VISIBLE
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
                { _, _, newPosition ->
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
    }
}