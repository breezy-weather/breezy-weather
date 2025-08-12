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

package org.breezyweather.ui.main.adapters.trend.daily

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import breezyweather.domain.location.model.Location
import org.breezyweather.R
import org.breezyweather.common.basic.BreezyActivity
import org.breezyweather.common.basic.models.options.appearance.DetailScreen
import org.breezyweather.common.extensions.formatTime
import org.breezyweather.common.extensions.getThemeColor
import org.breezyweather.ui.common.widgets.trend.TrendRecyclerView
import org.breezyweather.ui.common.widgets.trend.chart.PolylineAndHistogramView
import org.breezyweather.ui.theme.ThemeManager
import org.breezyweather.ui.theme.weatherView.WeatherViewController
import org.breezyweather.unit.formatting.UnitWidth
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

/**
 * Daily sunshine adapter.
 */
class DailySunshineAdapter(
    activity: BreezyActivity,
    location: Location,
) : AbsDailyTrendAdapter(activity, location) {
    private var mHighestIndex: Duration = 0.milliseconds

    inner class ViewHolder(itemView: View) : AbsDailyTrendAdapter.ViewHolder(itemView) {
        private val mPolylineAndHistogramView = PolylineAndHistogramView(itemView.context)

        init {
            dailyItem.chartItemView = mPolylineAndHistogramView
        }

        @SuppressLint("DefaultLocale")
        fun onBindView(
            activity: BreezyActivity,
            location: Location,
            position: Int,
        ) {
            val talkBackBuilder = StringBuilder(activity.getString(R.string.tag_sunshine))
            super.onBindView(activity, location, talkBackBuilder, position)
            val daily = location.weather!!.dailyForecast[position]

            val sunshineDuration = daily.sunshineDuration?.let {
                if (it > mHighestIndex) mHighestIndex else it
            }
            daily.sunshineDuration?.let {
                talkBackBuilder.append(activity.getString(org.breezyweather.unit.R.string.locale_separator))
                    .append(
                        it.formatTime(
                            context = itemView.context,
                            smallestUnit = DurationUnit.MINUTES,
                            unitWidth = UnitWidth.LONG
                        )
                    )
            }
            mPolylineAndHistogramView.setData(
                null,
                null,
                null,
                null,
                null,
                null,
                sunshineDuration?.inWholeMinutes?.toFloat(),
                daily.sunshineDuration?.formatTime(itemView.context),
                mHighestIndex.inWholeMinutes.toFloat(),
                0f
            )
            mPolylineAndHistogramView.setLineColors(
                if (sunshineDuration != null) {
                    ContextCompat.getColor(itemView.context, R.color.sunshine)
                } else {
                    Color.TRANSPARENT
                },
                if (sunshineDuration != null) {
                    ContextCompat.getColor(itemView.context, R.color.sunshine)
                } else {
                    Color.TRANSPARENT
                },
                activity.getThemeColor(com.google.android.material.R.attr.colorOutline)
            )

            val themeColors = ThemeManager.getInstance(itemView.context)
                .weatherThemeDelegate
                .getThemeColors(
                    itemView.context,
                    WeatherViewController.getWeatherKind(location),
                    WeatherViewController.isDaylight(location)
                )
            val lightTheme = ThemeManager.isLightTheme(itemView.context, location)
            mPolylineAndHistogramView.setShadowColors(themeColors[1], themeColors[2], lightTheme)
            mPolylineAndHistogramView.setTextColors(
                activity.getThemeColor(R.attr.colorTitleText),
                activity.getThemeColor(R.attr.colorBodyText),
                activity.getThemeColor(R.attr.colorTitleText)
            )
            mPolylineAndHistogramView.setHistogramAlpha(if (lightTheme) 1f else 0.5f)
            dailyItem.contentDescription = talkBackBuilder.toString()
            dailyItem.setOnClickListener {
                onItemClicked(activity, location, bindingAdapterPosition, DetailScreen.TAG_CLOUD_COVER)
            }
        }
    }

    init {
        mHighestIndex = location.weather!!.dailyForecast
            .mapNotNull { it.sun?.duration }
            .maxOrNull() ?: location.weather!!.dailyForecast
            .mapNotNull { it.sunshineDuration }
            .maxOrNull() ?: 0.milliseconds
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trend_daily, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: AbsDailyTrendAdapter.ViewHolder, position: Int) {
        (holder as ViewHolder).onBindView(activity, location, position)
    }

    override fun getItemCount() = location.weather!!.dailyForecast.size

    override fun isValid(location: Location) = location.weather!!.dailyForecast.any {
        (it.sunshineDuration?.inWholeMinutes ?: 0) > 0
    }

    override fun getDisplayName(context: Context) = context.getString(R.string.tag_sunshine)

    override fun bindBackgroundForHost(host: TrendRecyclerView) {
        host.setData(emptyList(), mHighestIndex.inWholeMinutes.toFloat(), 0f)
    }
}
