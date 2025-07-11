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
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.basic.models.options.appearance.HourlyTrendDisplay
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.common.widgets.trend.TrendRecyclerView
import org.breezyweather.ui.main.adapters.trend.hourly.AbsHourlyTrendAdapter
import org.breezyweather.ui.main.adapters.trend.hourly.HourlyAirQualityAdapter
import org.breezyweather.ui.main.adapters.trend.hourly.HourlyCloudCoverAdapter
import org.breezyweather.ui.main.adapters.trend.hourly.HourlyFeelsLikeAdapter
import org.breezyweather.ui.main.adapters.trend.hourly.HourlyHumidityAdapter
import org.breezyweather.ui.main.adapters.trend.hourly.HourlyPrecipitationAdapter
import org.breezyweather.ui.main.adapters.trend.hourly.HourlyPressureAdapter
import org.breezyweather.ui.main.adapters.trend.hourly.HourlyTemperatureAdapter
import org.breezyweather.ui.main.adapters.trend.hourly.HourlyUVAdapter
import org.breezyweather.ui.main.adapters.trend.hourly.HourlyVisibilityAdapter
import org.breezyweather.ui.main.adapters.trend.hourly.HourlyWindAdapter
import org.breezyweather.ui.theme.resource.ResourcesProviderFactory

@SuppressLint("NotifyDataSetChanged")
class HourlyTrendAdapter(
    private val activity: GeoActivity,
    private val host: TrendRecyclerView,
) : RecyclerView.Adapter<AbsHourlyTrendAdapter.ViewHolder>() {

    var adapters: Array<AbsHourlyTrendAdapter> = emptyArray()
        private set

    var selectedIndex = 0
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    private var selectedIndexCache = -1

    fun bindData(location: Location) {
        val provider = ResourcesProviderFactory.newInstance

        adapters = SettingsManager.getInstance(activity).hourlyTrendDisplayList.map {
            when (it) {
                HourlyTrendDisplay.TAG_TEMPERATURE -> HourlyTemperatureAdapter(
                    activity,
                    location,
                    provider,
                    SettingsManager.getInstance(activity).getTemperatureUnit(activity)
                )
                HourlyTrendDisplay.TAG_AIR_QUALITY -> HourlyAirQualityAdapter(
                    activity,
                    location
                )
                HourlyTrendDisplay.TAG_WIND -> HourlyWindAdapter(
                    activity,
                    location,
                    SettingsManager.getInstance(activity).getSpeedUnit(activity)
                )
                HourlyTrendDisplay.TAG_UV_INDEX -> HourlyUVAdapter(activity, location)
                HourlyTrendDisplay.TAG_PRECIPITATION -> HourlyPrecipitationAdapter(
                    activity,
                    location,
                    provider,
                    SettingsManager.getInstance(activity).getPrecipitationUnit(activity)
                )
                HourlyTrendDisplay.TAG_FEELS_LIKE -> HourlyFeelsLikeAdapter(
                    activity,
                    location,
                    provider,
                    SettingsManager.getInstance(activity).getTemperatureUnit(activity)
                )
                HourlyTrendDisplay.TAG_HUMIDITY -> HourlyHumidityAdapter(
                    activity,
                    location,
                    provider,
                    SettingsManager.getInstance(activity).getTemperatureUnit(activity)
                )
                HourlyTrendDisplay.TAG_PRESSURE -> HourlyPressureAdapter(
                    activity,
                    location,
                    provider,
                    SettingsManager.getInstance(activity).getPressureUnit(activity)
                )
                HourlyTrendDisplay.TAG_CLOUD_COVER -> HourlyCloudCoverAdapter(activity, location)
                HourlyTrendDisplay.TAG_VISIBILITY -> HourlyVisibilityAdapter(
                    activity,
                    location,
                    provider,
                    SettingsManager.getInstance(activity).getDistanceUnit(activity)
                )
            }
        }.filter {
            it.isValid(location)
        }.toTypedArray()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbsHourlyTrendAdapter.ViewHolder {
        return adapters[selectedIndex].onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: AbsHourlyTrendAdapter.ViewHolder, position: Int) {
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
