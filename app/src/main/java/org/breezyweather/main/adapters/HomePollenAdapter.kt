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

package org.breezyweather.main.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.breezyweather.R
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Daily
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.ui.composables.PollenGrid
import org.breezyweather.databinding.ItemPollenDailyBinding
import org.breezyweather.domain.weather.model.isIndexValid
import org.breezyweather.main.utils.MainThemeColorProvider
import org.breezyweather.theme.compose.BreezyWeatherTheme

open class HomePollenAdapter(
    private val location: Location
) : RecyclerView.Adapter<HomePollenViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomePollenViewHolder {
        return HomePollenViewHolder(
            ItemPollenDailyBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun onBindViewHolder(holder: HomePollenViewHolder, position: Int) {
        holder.onBindView(location, location.weather!!.dailyForecastStartingToday[position])
    }

    override fun getItemCount() = location.weather?.dailyForecastStartingToday?.filter { it.pollen?.isIndexValid == true }?.size ?: 0
}

class HomePollenViewHolder internal constructor(
    private val binding: ItemPollenDailyBinding
) : RecyclerView.ViewHolder(
    binding.root
) {
    @SuppressLint("SetTextI18n", "RestrictedApi")
    fun onBindView(location: Location, daily: Daily) {
        val context = itemView.context

        binding.title.text = daily.date.getFormattedDate(location.timeZone, context.getString(R.string.date_format_widget_long))
        binding.title.setTextColor(MainThemeColorProvider.getColor(location, R.attr.colorTitleText))

        daily.pollen?.let {
            binding.composeView.setContent {
                BreezyWeatherTheme(lightTheme = MainThemeColorProvider.isLightTheme(context, location)) {
                    PollenGrid(pollen = it)
                }
            }
        }

        itemView.setOnClickListener { }
    }
}