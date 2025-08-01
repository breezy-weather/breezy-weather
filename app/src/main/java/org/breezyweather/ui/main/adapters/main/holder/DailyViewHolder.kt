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
import org.breezyweather.common.basic.models.options.appearance.DailyTrendDisplay
import org.breezyweather.common.bus.EventBus
import org.breezyweather.common.extensions.getThemeColor
import org.breezyweather.common.extensions.isLandscape
import org.breezyweather.domain.settings.SettingsChangedMessage
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.common.adapters.ButtonAdapter
import org.breezyweather.ui.common.widgets.RecyclerViewNoVerticalScrollTouchListener
import org.breezyweather.ui.common.widgets.trend.TrendRecyclerView
import org.breezyweather.ui.main.adapters.trend.DailyTrendAdapter
import org.breezyweather.ui.main.layouts.TrendHorizontalLinearLayoutManager
import org.breezyweather.ui.main.widgets.TrendRecyclerViewScrollBar
import org.breezyweather.ui.theme.ThemeManager
import org.breezyweather.ui.theme.resource.providers.ResourceProvider
import androidx.lifecycle.Observer

class DailyViewHolder(parent: ViewGroup) : AbstractMainCardViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.container_main_daily_trend_card, parent, false)
) {
    private val subtitle: TextView = itemView.findViewById(R.id.daily_block_subtitle)
    private val buttonGroup: MaterialButtonGroup = itemView.findViewById(R.id.daily_block_button_group)
    private val trendRecyclerView: TrendRecyclerView = itemView.findViewById(R.id.daily_block_trendRecyclerView)
    private val scrollBar = TrendRecyclerViewScrollBar()

    private var currentTrendAdapter: DailyTrendAdapter? = null
    private var currentLocation: Location? = null
    private var currentSelectedTab: String? = null
    private var currentSetSelectedTab: ((String?) -> Unit)? = null
    private var settingsObserver: Observer<SettingsChangedMessage>? = null

    init {
        trendRecyclerView.setHasFixedSize(true)
        trendRecyclerView.addItemDecoration(scrollBar)

        // Listen for settings changes to update button group when dailyTrendDisplayList changes
        settingsObserver = Observer<SettingsChangedMessage> {
            currentLocation?.let { location ->
                updateButtonGroupFromSettings(location)
            }
        }
        EventBus.instance.with(SettingsChangedMessage::class.java).observeForever(settingsObserver!!)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindView(
        activity: BreezyActivity,
        location: Location,
        provider: ResourceProvider,
        listAnimationEnabled: Boolean,
        itemAnimationEnabled: Boolean,
        selectedTab: String?,
        setSelectedTab: (String?) -> Unit,
    ) {
        super.onBindView(activity, location, provider, listAnimationEnabled, itemAnimationEnabled)

        val weather = location.weather ?: return

        // Store current state for settings change updates
        currentLocation = location
        currentSelectedTab = selectedTab
        currentSetSelectedTab = setSelectedTab

        if (weather.current?.dailyForecast.isNullOrEmpty()) {
            subtitle.visibility = View.GONE
        } else {
            subtitle.visibility = View.VISIBLE
            subtitle.text = weather.current?.dailyForecast
        }

        currentTrendAdapter = DailyTrendAdapter(activity, trendRecyclerView).apply {
            bindData(location)
        }

        updateButtonGroupFromSettings(location)

        trendRecyclerView.layoutManager =
            TrendHorizontalLinearLayoutManager(
                context,
                if (context.isLandscape) 7 else 5,
                minHeight = context.resources.getDimensionPixelSize(R.dimen.daily_trend_item_height)
            )
        trendRecyclerView.setLineColor(
            context.getThemeColor(com.google.android.material.R.attr.colorOutline)
        )
        trendRecyclerView.setTextColor(
            ContextCompat.getColor(
                context,
                if (ThemeManager.isLightTheme(context, location)) {
                    R.color.colorTextGrey
                } else {
                    R.color.colorTextGrey2nd
                }
            )
        )
        trendRecyclerView.adapter = currentTrendAdapter
        trendRecyclerView.setKeyLineVisibility(
            SettingsManager.getInstance(context).isTrendHorizontalLinesEnabled
        )
        trendRecyclerView.addOnItemTouchListener(RecyclerViewNoVerticalScrollTouchListener())
        weather.todayIndex?.let { todayIndex ->
            trendRecyclerView.scrollToPosition(todayIndex)
        }
        scrollBar.resetColor(activity)
    }

    private fun updateButtonGroupFromSettings(location: Location) {
        val trendAdapter = currentTrendAdapter ?: return
        val setSelectedTab = currentSetSelectedTab ?: return

        // Re-bind data to get updated adapters from settings
        trendAdapter.bindData(location)

        val buttonList: MutableList<ButtonAdapter.Button> = trendAdapter.adapters.map {
            object : ButtonAdapter.Button {
                override val name = it.getDisplayName(context as BreezyActivity)
            }
        }.toMutableList()

        currentSelectedTab?.let { tab ->
            buttonList.indexOfFirst { it.name == tab }.let {
                if (it >= 0) {
                    trendAdapter.selectedIndex = it
                } else {
                    setSelectedTab(null) // Reset
                }
            }
        }

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
            buttonGroup.children
                .filter { it is MaterialButton && it.tag == MaterialButtonGroup.OVERFLOW_BUTTON_TAG }
                .forEach {
                    it.contentDescription = context.getString(R.string.action_more)
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
                            setSelectedTab(button.name)
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
    }

    fun cleanup() {
        settingsObserver?.let { observer ->
            EventBus.instance.with(SettingsChangedMessage::class.java).removeObserver(observer)
        }
        settingsObserver = null
        currentTrendAdapter = null
        currentLocation = null
        currentSelectedTab = null
        currentSetSelectedTab = null
    }

    override fun onRecycleView() {
        super.onRecycleView()
        cleanup()
    }
}
