/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

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
import org.breezyweather.common.basic.models.weather.Minutely
import org.breezyweather.common.extensions.DEFAULT_CARD_LIST_ITEM_ELEVATION_DP
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.isLandscape
import org.breezyweather.common.ui.adapters.TagAdapter
import org.breezyweather.common.ui.decorations.GridMarginsDecoration
import org.breezyweather.common.ui.widgets.precipitationBar.PrecipitationBar
import org.breezyweather.common.ui.widgets.trend.TrendRecyclerView
import org.breezyweather.common.utils.ColorUtils
import org.breezyweather.main.adapters.trend.HourlyTrendAdapter
import org.breezyweather.main.layouts.TrendHorizontalLinearLayoutManager
import org.breezyweather.main.utils.MainThemeColorProvider
import org.breezyweather.main.widgets.TrendRecyclerViewScrollBar
import org.breezyweather.settings.SettingsManager
import org.breezyweather.theme.ThemeManager
import org.breezyweather.theme.resource.providers.ResourceProvider
import org.breezyweather.theme.weatherView.WeatherViewController

private fun needToShowMinutelyForecast(minutelyList: List<Minutely>) =
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
    private val trendRecyclerView: TrendRecyclerView = itemView.findViewById(R.id.container_main_hourly_trend_card_trendRecyclerView)
    private val scrollBar: TrendRecyclerViewScrollBar = TrendRecyclerViewScrollBar()
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
        provider: ResourceProvider,
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
                WeatherViewController.getWeatherKind(weather),
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
        val tagList: MutableList<TagAdapter.Tag> = trendAdapter.adapters.map {
            object : TagAdapter.Tag {
                override val name = it.getDisplayName(activity)
            }
        }.toMutableList()

        if (tagList.size < 2) {
            tagView.visibility = View.GONE
        } else {
            val decorCount = tagView.itemDecorationCount
            for (i in 0 until decorCount) {
                tagView.removeItemDecorationAt(0)
            }
            tagView.addItemDecoration(
                GridMarginsDecoration(
                    context.resources.getDimension(R.dimen.little_margin),
                    context.resources.getDimension(R.dimen.normal_margin),
                    tagView
                )
            )
            tagView.layoutManager =
                TrendHorizontalLinearLayoutManager(context)
            tagView.adapter = TagAdapter(
                tagList,
                MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOnPrimary),
                MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOnSurface),
                MainThemeColorProvider.getColor(location, androidx.appcompat.R.attr.colorPrimary),
                ColorUtils.getWidgetSurfaceColor(
                    DEFAULT_CARD_LIST_ITEM_ELEVATION_DP,
                    MainThemeColorProvider.getColor(location, androidx.appcompat.R.attr.colorPrimary),
                    MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorSurface)
                ),
                { _, _, newPosition: Int ->
                    trendAdapter.selectedIndex = newPosition
                    return@TagAdapter false
                },
                0
            )
        }

        trendRecyclerView.layoutManager =
            TrendHorizontalLinearLayoutManager(
                context,
                if (context.isLandscape) 7 else 5
            )
        trendRecyclerView.setLineColor(MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOutline))
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
            minutelyStartText.text = minutelyList[0].date.getFormattedTime(location.timeZone, context.is12Hour)
            minutelyCenterText.text = minutelyList[(size - 1) / 2].date.getFormattedTime(location.timeZone, context.is12Hour)
            minutelyEndText.text = minutelyList[size - 1].date.getFormattedTime(location.timeZone, context.is12Hour)
            minutelyContainer.contentDescription =
                activity.getString(R.string.precipitation_between_time)
                    .replace("$1", minutelyList[0].date.getFormattedTime(location.timeZone, context.is12Hour))
                    .replace("$2", minutelyList[size - 1].date.getFormattedTime(location.timeZone, context.is12Hour))
        } else {
            minutelyContainer.visibility = View.GONE
        }

        minutelyTitle.setTextColor(colors[0])

        precipitationBar.precipitationColor = ThemeManager
            .getInstance(context)
            .weatherThemeDelegate
            .getThemeColors(
                context,
                WeatherViewController.getWeatherKind(weather),
                location.isDaylight
            )[0]
        precipitationBar.subLineColor = MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOutline)
        precipitationBar.highlightColor = MainThemeColorProvider.getColor(location, androidx.appcompat.R.attr.colorPrimary)
        precipitationBar.textColor = MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOnPrimary)
        precipitationBar.setShadowColors(colors[0], colors[1], MainThemeColorProvider.isLightTheme(itemView.context, location))

        minutelyStartText.setTextColor(MainThemeColorProvider.getColor(location, R.attr.colorBodyText))
        minutelyCenterText.setTextColor(MainThemeColorProvider.getColor(location, R.attr.colorBodyText))
        minutelyEndText.setTextColor(MainThemeColorProvider.getColor(location, R.attr.colorBodyText))

        minutelyStartLine.setBackgroundColor(MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOutline))
        minutelyEndLine.setBackgroundColor(MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOutline))
    }
}