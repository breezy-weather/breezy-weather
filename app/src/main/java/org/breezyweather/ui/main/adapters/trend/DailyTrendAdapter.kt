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

package org.breezyweather.ui.main.adapters.trend

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import breezyweather.domain.location.model.Location
import org.breezyweather.common.basic.BreezyActivity
import org.breezyweather.common.basic.models.options.appearance.DailyTrendDisplay
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.common.widgets.trend.TrendRecyclerView
import org.breezyweather.ui.main.adapters.trend.daily.AbsDailyTrendAdapter
import org.breezyweather.ui.main.adapters.trend.daily.DailyAirQualityAdapter
import org.breezyweather.ui.main.adapters.trend.daily.DailyFeelsLikeAdapter
import org.breezyweather.ui.main.adapters.trend.daily.DailyPrecipitationAdapter
import org.breezyweather.ui.main.adapters.trend.daily.DailySunshineAdapter
import org.breezyweather.ui.main.adapters.trend.daily.DailyTemperatureAdapter
import org.breezyweather.ui.main.adapters.trend.daily.DailyUVAdapter
import org.breezyweather.ui.main.adapters.trend.daily.DailyWindAdapter
import org.breezyweather.ui.theme.resource.ResourcesProviderFactory

@SuppressLint("NotifyDataSetChanged")
class DailyTrendAdapter(
    private val activity: BreezyActivity,
    private val host: TrendRecyclerView,
) : RecyclerView.Adapter<AbsDailyTrendAdapter.ViewHolder>() {

    var adapters: Array<AbsDailyTrendAdapter> = emptyArray()
        private set

    var selectedIndex: Int = 0
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    private var selectedIndexCache = -1

    fun bindData(location: Location) {
        val provider = ResourcesProviderFactory.newInstance

        adapters = SettingsManager.getInstance(activity).dailyTrendDisplayList.map {
            when (it) {
                DailyTrendDisplay.TAG_TEMPERATURE -> DailyTemperatureAdapter(
                    activity,
                    location,
                    provider,
                    SettingsManager.getInstance(activity).getTemperatureUnit(activity)
                )
                DailyTrendDisplay.TAG_AIR_QUALITY -> DailyAirQualityAdapter(
                    activity,
                    location
                )
                DailyTrendDisplay.TAG_WIND -> DailyWindAdapter(
                    activity,
                    location,
                    SettingsManager.getInstance(activity).getSpeedUnit(activity)
                )
                DailyTrendDisplay.TAG_UV_INDEX -> DailyUVAdapter(activity, location)
                DailyTrendDisplay.TAG_PRECIPITATION -> DailyPrecipitationAdapter(
                    activity,
                    location,
                    provider,
                    SettingsManager.getInstance(activity).getPrecipitationUnit(activity)
                )
                DailyTrendDisplay.TAG_SUNSHINE -> DailySunshineAdapter(
                    activity,
                    location
                )
                DailyTrendDisplay.TAG_FEELS_LIKE -> DailyFeelsLikeAdapter(
                    activity,
                    location,
                    provider,
                    SettingsManager.getInstance(activity).getTemperatureUnit(activity)
                )
            }
        }.filter {
            it.isValid(location)
        }.toTypedArray()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbsDailyTrendAdapter.ViewHolder {
        return adapters[selectedIndex].onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: AbsDailyTrendAdapter.ViewHolder, position: Int) {
        adapters[selectedIndex].onBindViewHolder(holder, position)
    }

    override fun getItemCount() = adapters.getOrNull(selectedIndex)?.itemCount ?: 0

    override fun getItemViewType(position: Int): Int {
        if (selectedIndexCache != selectedIndex) {
            selectedIndexCache = selectedIndex
            adapters[selectedIndex].bindBackgroundForHost(host)
        }
        return selectedIndex
    }
}
