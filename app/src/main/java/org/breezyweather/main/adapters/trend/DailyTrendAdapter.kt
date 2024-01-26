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

package org.breezyweather.main.adapters.trend

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.appearance.DailyTrendDisplay
import org.breezyweather.common.ui.widgets.trend.TrendRecyclerView
import org.breezyweather.main.adapters.trend.daily.*
import org.breezyweather.settings.SettingsManager
import org.breezyweather.theme.resource.ResourcesProviderFactory

@SuppressLint("NotifyDataSetChanged")
class DailyTrendAdapter(
    private val activity: GeoActivity,
    private val host: TrendRecyclerView,
) : RecyclerView.Adapter<AbsDailyTrendAdapter.ViewHolder>() {

    var adapters: Array<AbsDailyTrendAdapter> = emptyArray()
        private set

    var selectedIndex = 0
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
                    SettingsManager.getInstance(activity).temperatureUnit
                )
                DailyTrendDisplay.TAG_AIR_QUALITY -> DailyAirQualityAdapter(
                    activity,
                    location
                )
                DailyTrendDisplay.TAG_WIND -> DailyWindAdapter(
                    activity,
                    location,
                    SettingsManager.getInstance(activity).speedUnit
                )
                DailyTrendDisplay.TAG_UV_INDEX -> DailyUVAdapter(activity, location)
                DailyTrendDisplay.TAG_PRECIPITATION -> DailyPrecipitationAdapter(
                    activity,
                    location,
                    provider,
                    SettingsManager.getInstance(activity).precipitationUnit
                )
                DailyTrendDisplay.TAG_FEELS_LIKE -> DailyFeelsLikeAdapter(
                    activity,
                    location,
                    provider,
                    SettingsManager.getInstance(activity).temperatureUnit
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