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
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.UV
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.basic.models.options.appearance.DetailScreen
import org.breezyweather.common.basic.models.options.basic.Utils
import org.breezyweather.domain.weather.model.getContentDescription
import org.breezyweather.domain.weather.model.getUVColor
import org.breezyweather.ui.common.widgets.trend.TrendRecyclerView
import org.breezyweather.ui.common.widgets.trend.chart.PolylineAndHistogramView
import org.breezyweather.ui.main.utils.MainThemeColorProvider
import org.breezyweather.ui.theme.ThemeManager
import org.breezyweather.ui.theme.weatherView.WeatherViewController
import kotlin.math.roundToInt

/**
 * Daily UV adapter.
 */
class DailyUVAdapter(
    activity: GeoActivity,
    location: Location,
) : AbsDailyTrendAdapter(activity, location) {
    private var mHighestIndex: Float = 0f

    inner class ViewHolder(itemView: View) : AbsDailyTrendAdapter.ViewHolder(itemView) {
        private val mPolylineAndHistogramView = PolylineAndHistogramView(itemView.context)

        init {
            dailyItem.chartItemView = mPolylineAndHistogramView
        }

        @SuppressLint("SetTextI18n, InflateParams", "DefaultLocale")
        fun onBindView(activity: GeoActivity, location: Location, position: Int) {
            val talkBackBuilder = StringBuilder(activity.getString(R.string.tag_uv))
            super.onBindView(activity, location, talkBackBuilder, position)
            val weather = location.weather!!
            val daily = weather.dailyForecast[position]

            val index = daily.uV?.index
            if (index != null) {
                talkBackBuilder.append(activity.getString(R.string.comma_separator))
                    .append(daily.uV!!.getContentDescription(activity))
            }
            mPolylineAndHistogramView.setData(
                null, null,
                null, null,
                null, null,
                index?.roundToInt()?.toFloat() ?: 0f, index?.let { Utils.formatDouble(activity, it, 0) },
                mHighestIndex, 0f
            )
            mPolylineAndHistogramView.setLineColors(
                daily.uV?.getUVColor(activity) ?: Color.TRANSPARENT,
                daily.uV?.getUVColor(activity) ?: Color.TRANSPARENT,
                MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOutline)
            )

            val themeColors = ThemeManager.getInstance(itemView.context)
                .weatherThemeDelegate
                .getThemeColors(
                    itemView.context,
                    WeatherViewController.getWeatherKind(location),
                    WeatherViewController.isDaylight(location)
                )
            val lightTheme = MainThemeColorProvider.isLightTheme(itemView.context, location)
            mPolylineAndHistogramView.setShadowColors(themeColors[1], themeColors[2], lightTheme)
            mPolylineAndHistogramView.setTextColors(
                MainThemeColorProvider.getColor(location, R.attr.colorTitleText),
                MainThemeColorProvider.getColor(location, R.attr.colorBodyText),
                MainThemeColorProvider.getColor(location, R.attr.colorTitleText)
            )
            mPolylineAndHistogramView.setHistogramAlpha(if (lightTheme) 1f else 0.5f)
            dailyItem.contentDescription = talkBackBuilder.toString()
            dailyItem.setOnClickListener {
                onItemClicked(activity, location, bindingAdapterPosition, DetailScreen.TAG_UV_INDEX)
            }
        }
    }

    init {
        mHighestIndex = (location.weather!!.dailyForecast.mapNotNull { it.uV?.index }.maxOrNull() ?: 0.0).toFloat()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trend_daily, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: AbsDailyTrendAdapter.ViewHolder, position: Int) {
        (holder as ViewHolder).onBindView(activity, location, position)
    }

    override fun getItemCount() = location.weather!!.dailyForecast.size

    override fun isValid(location: Location) = mHighestIndex > 0

    override fun getDisplayName(context: Context) = context.getString(R.string.tag_uv)

    override fun bindBackgroundForHost(host: TrendRecyclerView) {
        val keyLineList = mutableListOf<TrendRecyclerView.KeyLine>()
        keyLineList.add(
            TrendRecyclerView.KeyLine(
                UV.UV_INDEX_MIDDLE.toFloat(),
                Utils.formatDouble(activity, UV.UV_INDEX_MIDDLE, 0),
                activity.getString(R.string.uv_alert_level),
                TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
            )
        )
        host.setData(keyLineList, mHighestIndex, 0f)
    }
}
