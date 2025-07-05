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
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.children
import breezyweather.domain.location.model.Location
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonGroup
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.extensions.isLandscape
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.common.adapters.TagAdapter
import org.breezyweather.ui.common.widgets.trend.TrendRecyclerView
import org.breezyweather.ui.main.adapters.trend.DailyTrendAdapter
import org.breezyweather.ui.main.layouts.TrendHorizontalLinearLayoutManager
import org.breezyweather.ui.main.utils.MainThemeColorProvider
import org.breezyweather.ui.main.widgets.TrendRecyclerViewScrollBar
import org.breezyweather.ui.theme.resource.providers.ResourceProvider

class DailyViewHolder(parent: ViewGroup) : AbstractMainCardViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.container_main_daily_trend_card, parent, false)
) {
    private val titleView: TextView = itemView.findViewById(R.id.daily_block_title)
    private val titleIconView: ImageView = itemView.findViewById(R.id.daily_block_title_icon)
    private val subtitle: TextView = itemView.findViewById(R.id.daily_block_subtitle)
    private val buttonGroup: MaterialButtonGroup = itemView.findViewById(R.id.daily_block_button_group)
    private val trendRecyclerView: TrendRecyclerView = itemView.findViewById(R.id.daily_block_trendRecyclerView)
    private val scrollBar = TrendRecyclerViewScrollBar()

    init {
        trendRecyclerView.setHasFixedSize(true)
        trendRecyclerView.addItemDecoration(scrollBar)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindView(
        activity: GeoActivity,
        location: Location,
        provider: ResourceProvider,
        listAnimationEnabled: Boolean,
        itemAnimationEnabled: Boolean,
    ) {
        super.onBindView(activity, location, provider, listAnimationEnabled, itemAnimationEnabled)

        val color = MainThemeColorProvider.getColor(location, R.attr.colorTitleText)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            titleView.isAccessibilityHeading = true
        }
        titleView.setText(R.string.daily_forecast)
        titleView.setTextColor(color)
        titleIconView.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_calendar))
        titleIconView.setColorFilter(color)

        val weather = location.weather ?: return

        if (weather.current?.dailyForecast.isNullOrEmpty()) {
            subtitle.visibility = View.GONE
        } else {
            subtitle.visibility = View.VISIBLE
            subtitle.text = weather.current?.dailyForecast
        }

        val trendAdapter = DailyTrendAdapter(activity, trendRecyclerView).apply {
            bindData(location)
        }
        val tagList: MutableList<TagAdapter.Tag> = trendAdapter.adapters.map {
            object : TagAdapter.Tag {
                override val name = it.getDisplayName(activity)
            }
        }.toMutableList()

        if (tagList.size < 2) {
            buttonGroup.visibility = View.GONE
        } else {
            buttonGroup.visibility = View.VISIBLE
            val buttons = buttonGroup.children.filter { it is MaterialButton }
            buttons.forEachIndexed { index, button ->
                tagList.getOrElse(index) { null }?.let { tag ->
                    if (button.tag != MaterialButtonGroup.OVERFLOW_BUTTON_TAG) {
                        (button as MaterialButton).apply {
                            visibility = View.VISIBLE
                            text = tag.name
                            isCheckable = true
                            isChecked = index == 0
                            setOnClickListener {
                                trendAdapter.selectedIndex = index
                                buttons.forEach { button -> (button as MaterialButton).isChecked = false }
                                isChecked = true
                            }
                        }
                    }
                } ?: run {
                    button.visibility = View.GONE
                }
            }
        }
        trendRecyclerView.layoutManager =
            TrendHorizontalLinearLayoutManager(
                context,
                if (context.isLandscape) 7 else 5
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
        weather.todayIndex?.let { todayIndex ->
            trendRecyclerView.scrollToPosition(todayIndex)
        }
        scrollBar.resetColor(location)
    }
}
