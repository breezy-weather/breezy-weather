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

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.WeatherCode
import org.breezyweather.R
import org.breezyweather.common.basic.BreezyActivity
import org.breezyweather.common.basic.models.options.appearance.DetailScreen
import org.breezyweather.common.basic.models.options.basic.UnitUtils
import org.breezyweather.common.extensions.areBlocksSquished
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.theme.resource.ResourceHelper
import org.breezyweather.ui.theme.resource.providers.ResourceProvider
import java.util.Calendar
import java.util.Date

class PrecipitationViewHolder(parent: ViewGroup) : AbstractMainCardViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.container_main_precipitation, parent, false)
) {
    private val precipitationValueView: TextView = itemView.findViewById(R.id.precipitation_value)
    private val precipitationAmountView: TextView = itemView.findViewById(R.id.precipitation_amount)
    private val precipitationIconView: ImageView = itemView.findViewById(R.id.precipitation_icon)

    override fun onBindView(
        activity: BreezyActivity,
        location: Location,
        provider: ResourceProvider,
        listAnimationEnabled: Boolean,
        itemAnimationEnabled: Boolean,
    ) {
        super.onBindView(activity, location, provider, listAnimationEnabled, itemAnimationEnabled)

        val talkBackBuilder = StringBuilder(context.getString(R.string.precipitation))
        location.weather?.let { weather ->
            val cal = Date().toCalendarWithTimeZone(location.javaTimeZone)
            val currentHour = cal[Calendar.HOUR_OF_DAY]

            // Early morning
            val precipitation = if (currentHour < 5) {
                weather.dailyForecast.getOrElse(weather.todayIndex!!.minus(1)) { null }?.night?.precipitation
            } else if (currentHour < 17) {
                weather.today?.day?.precipitation
            } else {
                weather.today?.night?.precipitation
            }

            val isDay = currentHour in 5..16

            var isSnow = false
            precipitationAmountView.text = if ((precipitation?.rain ?: 0.0) > 0 && (precipitation?.snow ?: 0.0) > 0) {
                context.getString(
                    if (isDay) R.string.precipitation_total_day else R.string.precipitation_total_night
                )
            } else if ((precipitation?.snow ?: 0.0) > 0) {
                context.getString(
                    if ((precipitation?.snow ?: 0.0) == (precipitation?.total ?: 0.0)) {
                        isSnow = true
                        if (isDay) R.string.precipitation_snow_total_day else R.string.precipitation_snow_total_night
                    } else {
                        if (isDay) R.string.precipitation_total_day else R.string.precipitation_total_night
                    }
                )
            } else if ((precipitation?.rain ?: 0.0) > 0) {
                context.getString(
                    if ((precipitation?.rain ?: 0.0) == (precipitation?.total ?: 0.0)) {
                        if (isDay) R.string.precipitation_rain_total_day else R.string.precipitation_rain_total_night
                    } else {
                        if (isDay) R.string.precipitation_total_day else R.string.precipitation_total_night
                    }
                )
            } else {
                context.getString(
                    if (isDay) R.string.precipitation_total_day else R.string.precipitation_total_night
                )
            }
            precipitationAmountView.maxLines = if (itemView.context.areBlocksSquished) 2 else 3

            precipitation?.total?.let { total ->
                val precipitationUnit = if (isSnow) {
                    SettingsManager.getInstance(context).getSnowfallUnit(context)
                } else {
                    SettingsManager.getInstance(context).getPrecipitationUnit(context)
                }
                precipitationValueView.text = UnitUtils.formatUnitsHalfSize(
                    precipitationUnit.formatMeasure(context, total)
                )

                talkBackBuilder.append(context.getString(R.string.colon_separator))
                talkBackBuilder.append(precipitationAmountView.text)
                talkBackBuilder.append(context.getString(R.string.colon_separator))
                talkBackBuilder.append(precipitationUnit.formatContentDescription(context, total))
            } ?: run {
                precipitationValueView.text = "-"
            }

            precipitationIconView.setImageDrawable(
                ResourceHelper.getWeatherIcon(
                    provider,
                    if ((precipitation?.rain ?: 0.0) > 0 && (precipitation?.snow ?: 0.0) > 0) {
                        WeatherCode.SLEET
                    } else if ((precipitation?.snow ?: 0.0) > 0) {
                        WeatherCode.SNOW
                    } else {
                        WeatherCode.RAIN
                    },
                    isDay
                )
            )
        }

        itemView.contentDescription = talkBackBuilder.toString()
        itemView.setOnClickListener {
            IntentHelper.startDailyWeatherActivity(
                context as BreezyActivity,
                location.formattedId,
                location.weather!!.todayIndex,
                DetailScreen.TAG_PRECIPITATION
            )
        }
    }
}
