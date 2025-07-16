/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.ui.main.adapters.main.holder

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import breezyweather.domain.location.model.Location
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonGroup
import org.breezyweather.R
import org.breezyweather.common.basic.BreezyActivity
import org.breezyweather.common.extensions.isLandscape
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.common.adapters.ButtonAdapter
import org.breezyweather.ui.common.widgets.RecyclerViewNoVerticalScrollTouchListener
import org.breezyweather.ui.common.widgets.trend.TrendRecyclerView
import org.breezyweather.ui.main.adapters.trend.DailyTrendAdapter
import org.breezyweather.ui.main.layouts.TrendHorizontalLinearLayoutManager
import org.breezyweather.ui.main.utils.MainThemeColorProvider
import org.breezyweather.ui.main.widgets.TrendRecyclerViewScrollBar
import org.breezyweather.ui.theme.ThemeManager
import org.breezyweather.ui.theme.resource.providers.ResourceProvider
import org.breezyweather.ui.theme.weatherView.WeatherViewController

class DailyViewHolder(parent: ViewGroup) : AbstractMainCardViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.container_main_daily_trend_card, parent, false)
) {
    private val title: TextView = itemView.findViewById(R.id.container_main_daily_trend_card_title)
    private val subtitle: TextView = itemView.findViewById(R.id.container_main_daily_trend_card_subtitle)
    private val buttonGroup: MaterialButtonGroup =
        itemView.findViewById(R.id.container_main_daily_trend_card_buttonView)
    private val trendRecyclerView: TrendRecyclerView = itemView.findViewById(
        R.id.container_main_daily_trend_card_trendRecyclerView
    )
    private val scrollBar = TrendRecyclerViewScrollBar()

    init {
        trendRecyclerView.setHasFixedSize(true)
        trendRecyclerView.addItemDecoration(scrollBar)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindView(
        activity: BreezyActivity,
        location: Location,
        provider: ResourceProvider,
        listAnimationEnabled: Boolean,
        itemAnimationEnabled: Boolean,
        firstCard: Boolean,
    ) {
        super.onBindView(activity, location, provider, listAnimationEnabled, itemAnimationEnabled, firstCard)

        val weather = location.weather ?: return
        val colors = ThemeManager
            .getInstance(context)
            .weatherThemeDelegate
            .getThemeColors(
                context,
                WeatherViewController.getWeatherKind(location),
                WeatherViewController.isDaylight(location)
            )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            title.isAccessibilityHeading = true
        }
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
        val buttonList: MutableList<ButtonAdapter.Button> = trendAdapter.adapters.map {
            object : ButtonAdapter.Button {
                override val name = it.getDisplayName(activity)
            }
        }.toMutableList()

        if (buttonList.size < 2) {
            buttonGroup.visibility = View.GONE
        } else {
            buttonGroup.visibility = View.VISIBLE
            // Dirty trick to get the button group to actually redraw with the correct styles AND the overflow menu
            while (
                buttonGroup.children
                    .filter { it is MaterialButton && it.tag != MaterialButtonGroup.OVERFLOW_BUTTON_TAG }
                    .count() != 0
            ) {
                buttonGroup.children
                    .filter { it is MaterialButton && it.tag != MaterialButtonGroup.OVERFLOW_BUTTON_TAG }
                    .forEach {
                        buttonGroup.removeView(it)
                    }
            }
            buttonList.forEachIndexed { index, button ->
                buttonGroup.addView(
                    MaterialButton(
                        context,
                        null,
                        com.google.android.material.R.attr.materialButtonStyle
                    ).apply {
                        text = button.name
                        isCheckable = true
                        isChecked = index == trendAdapter.selectedIndex
                        setOnClickListener {
                            trendAdapter.selectedIndex = index
                            buttonGroup.children
                                .filter { it is MaterialButton && it.tag != MaterialButtonGroup.OVERFLOW_BUTTON_TAG }
                                .forEach { button ->
                                    (button as MaterialButton).isChecked = false
                                }
                            isChecked = true
                        }
                    }
                )
            }
        }
        trendRecyclerView.layoutManager =
            TrendHorizontalLinearLayoutManager(
                context,
                if (context.isLandscape) 7 else 5,
                minHeight = context.resources.getDimensionPixelSize(R.dimen.daily_trend_item_height)
            )
        trendRecyclerView.setLineColor(
            MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOutline)
        )
        trendRecyclerView.setTextColor(
            ContextCompat.getColor(
                context,
                if (MainThemeColorProvider.isLightTheme(context, location)) {
                    R.color.colorTextGrey
                } else {
                    R.color.colorTextGrey2nd
                }
            )
        )
        trendRecyclerView.adapter = trendAdapter
        trendRecyclerView.setKeyLineVisibility(
            SettingsManager.getInstance(context).isTrendHorizontalLinesEnabled
        )
        trendRecyclerView.addOnItemTouchListener(RecyclerViewNoVerticalScrollTouchListener())
        weather.todayIndex?.let { todayIndex ->
            trendRecyclerView.scrollToPosition(todayIndex)
        }
        scrollBar.resetColor(location)
    }
}
