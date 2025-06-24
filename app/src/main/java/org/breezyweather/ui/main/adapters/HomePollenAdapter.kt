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

package org.breezyweather.ui.main.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Daily
import kotlinx.collections.immutable.ImmutableSet
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.basic.models.options.appearance.ChartDisplay
import org.breezyweather.common.extensions.capitalize
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.getLongWeekdayDayMonth
import org.breezyweather.common.source.PollenIndexSource
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.databinding.ItemPollenDailyBinding
import org.breezyweather.domain.weather.index.PollenIndex
import org.breezyweather.domain.weather.model.isIndexValid
import org.breezyweather.ui.common.composables.PollenGrid
import org.breezyweather.ui.main.utils.MainThemeColorProvider
import org.breezyweather.ui.theme.compose.BreezyWeatherTheme

open class HomePollenAdapter(
    private val location: Location,
    private val pollenIndexSource: PollenIndexSource?,
    private val specificPollens: ImmutableSet<PollenIndex>,
) : RecyclerView.Adapter<HomePollenViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomePollenViewHolder {
        return HomePollenViewHolder(
            ItemPollenDailyBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun onBindViewHolder(holder: HomePollenViewHolder, position: Int) {
        holder.onBindView(
            location,
            location.weather!!.dailyForecastStartingToday[position],
            pollenIndexSource,
            specificPollens
        )
    }

    override fun getItemCount() = location.weather?.dailyForecastStartingToday?.filter {
        it.pollen?.isIndexValid == true
    }?.size ?: 0
}

class HomePollenViewHolder internal constructor(
    private val binding: ItemPollenDailyBinding,
) : RecyclerView.ViewHolder(binding.root) {
    @SuppressLint("SetTextI18n", "RestrictedApi")
    fun onBindView(
        location: Location,
        daily: Daily,
        pollenIndexSource: PollenIndexSource?,
        specificPollens: ImmutableSet<PollenIndex>,
    ) {
        val context = itemView.context

        binding.title.text = daily.date
            .getFormattedDate(getLongWeekdayDayMonth(context), location, context)
            .capitalize(context.currentLocale)
        binding.title.setTextColor(MainThemeColorProvider.getColor(location, R.attr.colorTitleText))

        daily.pollen?.let {
            binding.composeView.setContent {
                BreezyWeatherTheme(lightTheme = MainThemeColorProvider.isLightTheme(context, location)) {
                    PollenGrid(
                        pollen = it,
                        pollenIndexSource = pollenIndexSource,
                        specificPollens = specificPollens
                    )
                }
            }
        }

        itemView.setOnClickListener {
            IntentHelper.startDailyWeatherActivity(
                context as GeoActivity,
                location.formattedId,
                location.weather!!.dailyForecast.indexOf(daily),
                ChartDisplay.TAG_POLLEN
            )
        }
    }
}
