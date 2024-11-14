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

package org.breezyweather.main.adapters.main.holder

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Current
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.basic.models.options.appearance.DetailDisplay
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.extensions.isLandscape
import org.breezyweather.domain.location.model.isDaylight
import org.breezyweather.main.utils.MainThemeColorProvider
import org.breezyweather.settings.SettingsManager
import org.breezyweather.theme.ThemeManager
import org.breezyweather.theme.compose.BreezyWeatherTheme
import org.breezyweather.theme.compose.DayNightTheme
import org.breezyweather.theme.resource.providers.ResourceProvider
import org.breezyweather.theme.weatherView.WeatherViewController

class DetailsViewHolder(parent: ViewGroup) : AbstractMainCardViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.container_main_details, parent, false)
) {
    private val mTitle: TextView = itemView.findViewById(R.id.container_main_details_title)
    private val mTime: TextView = itemView.findViewById(R.id.container_main_details_time)
    private val mDetailsList: ComposeView = itemView.findViewById(R.id.container_main_details_list)

    override fun onBindView(
        activity: GeoActivity,
        location: Location,
        provider: ResourceProvider,
        listAnimationEnabled: Boolean,
        itemAnimationEnabled: Boolean,
        firstCard: Boolean,
    ) {
        super.onBindView(activity, location, provider, listAnimationEnabled, itemAnimationEnabled, firstCard)
        location.weather?.let { weather ->
            weather.current?.let { current ->
                mTitle.setTextColor(
                    ThemeManager.getInstance(context)
                        .weatherThemeDelegate
                        .getThemeColors(
                            context,
                            WeatherViewController.getWeatherKind(location),
                            WeatherViewController.isDaylight(location)
                        )[0]
                )
                mTime.text = weather.base.mainUpdateTime?.getFormattedTime(location, context, context.is12Hour)
                mDetailsList.setContent {
                    BreezyWeatherTheme(lightTheme = MainThemeColorProvider.isLightTheme(context, location)) {
                        ContentView(
                            SettingsManager.getInstance(context).detailDisplayList,
                            SettingsManager.getInstance(context).detailDisplayUnlisted,
                            current,
                            location
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun ContentView(
        detailsInHeaderList: List<DetailDisplay>,
        detailsNotInHeaderList: List<DetailDisplay>,
        current: Current,
        location: Location,
    ) {
        // TODO: Lazy
        Column {
            availableDetails(
                LocalContext.current,
                detailsInHeaderList,
                detailsNotInHeaderList,
                current,
                location.isDaylight
            ).forEach { detailDisplay ->
                ListItem(
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent
                    ),
                    headlineContent = {
                        Text(
                            detailDisplay.getName(LocalContext.current),
                            fontWeight = FontWeight.Bold,
                            color = DayNightTheme.colors.titleColor
                        )
                    },
                    supportingContent = {
                        Text(
                            detailDisplay.getCurrentValue(LocalContext.current, current, location.isDaylight)!!,
                            color = DayNightTheme.colors.bodyColor
                        )
                    },
                    leadingContent = {
                        Icon(
                            painterResource(detailDisplay.iconId),
                            contentDescription = detailDisplay.getName(LocalContext.current),
                            tint = DayNightTheme.colors.titleColor
                        )
                    }
                )
            }
        }
    }

    companion object {
        fun availableDetails(
            context: Context,
            detailsInHeaderList: List<DetailDisplay>,
            detailsNotInHeaderList: List<DetailDisplay>,
            current: Current,
            isDaylight: Boolean,
        ): List<DetailDisplay> {
            val detailsInHeaderNotNullList = detailsInHeaderList.filter {
                it.getCurrentValue(context, current, isDaylight) != null
            }
            val detailsNotInHeaderNotNullList = detailsNotInHeaderList.filter {
                it.getCurrentValue(context, current, isDaylight) != null
            }
            val nbMaxInHeader = if (context.isLandscape) {
                HeaderViewHolder.NB_CURRENT_ITEMS_LANDSCAPE
            } else {
                HeaderViewHolder.NB_CURRENT_ITEMS_PORTRAIT
            }
            return if (detailsInHeaderNotNullList.size > nbMaxInHeader) {
                detailsInHeaderNotNullList.subList(nbMaxInHeader, detailsInHeaderNotNullList.size) +
                    detailsNotInHeaderNotNullList
            } else {
                detailsNotInHeaderNotNullList
            }
        }
    }
}
