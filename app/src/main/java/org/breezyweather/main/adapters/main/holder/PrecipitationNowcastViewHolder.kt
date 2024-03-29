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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import breezyweather.domain.location.model.Location
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.ui.widgets.precipitationBar.PrecipitationBar
import org.breezyweather.domain.weather.model.getMinutelyDescription
import org.breezyweather.domain.weather.model.getMinutelyTitle
import org.breezyweather.domain.weather.model.hasMinutelyPrecipitation
import org.breezyweather.main.utils.MainThemeColorProvider
import org.breezyweather.settings.SettingsManager
import org.breezyweather.theme.ThemeManager
import org.breezyweather.theme.resource.providers.ResourceProvider
import org.breezyweather.theme.weatherView.WeatherViewController

class PrecipitationNowcastViewHolder(
    parent: ViewGroup
) : AbstractMainCardViewHolder(
    LayoutInflater
        .from(parent.context)
        .inflate(R.layout.container_main_precipitation_nowcast_card, parent, false)
) {
    private val minutelyContainer: LinearLayout = itemView.findViewById(R.id.container_main_minutely_card_minutely)
    private val minutelyTitle: TextView = itemView.findViewById(R.id.container_main_minutely_card_title)
    private val minutelySubtitle: TextView = itemView.findViewById(R.id.container_main_minutely_card_subtitle)
    private val precipitationBar: PrecipitationBar = itemView.findViewById(R.id.container_main_minutely_card_minutelyBar)
    private val minutelyStartText: TextView = itemView.findViewById(R.id.container_main_minutely_card_minutelyStartText)
    private val minutelyCenterText: TextView = itemView.findViewById(R.id.container_main_minutely_card_minutelyCenterText)
    private val minutelyEndText: TextView = itemView.findViewById(R.id.container_main_minutely_card_minutelyEndText)
    private val minutelyStartLine: View = itemView.findViewById(R.id.container_main_minutely_card_minutelyStartLine)
    private val minutelyEndLine: View = itemView.findViewById(R.id.container_main_minutely_card_minutelyEndLine)

    init {
        minutelyContainer.setOnClickListener { /* do nothing. */ }
    }

    override fun onBindView(
        activity: GeoActivity,
        location: Location,
        provider: ResourceProvider,
        listAnimationEnabled: Boolean,
        itemAnimationEnabled: Boolean,
        firstCard: Boolean
    ) {
        super.onBindView(
            activity,
            location,
            provider,
            listAnimationEnabled,
            itemAnimationEnabled,
            firstCard
        )

        val weather = location.weather ?: return
        val colors = ThemeManager
            .getInstance(context)
            .weatherThemeDelegate
            .getThemeColors(
                context,
                WeatherViewController.getWeatherKind(location),
                WeatherViewController.isDaylight(location)
            )

        val minutelyList = weather.minutelyForecast
        if (minutelyList.size >= 3 && weather.hasMinutelyPrecipitation) {
            minutelyContainer.visibility = View.VISIBLE
            precipitationBar.precipitationIntensities = minutelyList.map {
                it.precipitationIntensity ?: 0.0
            }.toTypedArray()
            precipitationBar.indicatorGenerator = object : PrecipitationBar.IndicatorGenerator {
                override fun getIndicatorContent(precipitation: Double) =
                    SettingsManager
                        .getInstance(activity)
                        .precipitationIntensityUnit
                        .getValueText(activity, precipitation)
            }

            val size = minutelyList.size
            minutelyStartText.text = minutelyList[0].date.getFormattedTime(location, context, context.is12Hour)
            minutelyCenterText.text = minutelyList[(size - 1) / 2].date.getFormattedTime(location, context, context.is12Hour)
            minutelyEndText.text = minutelyList[size - 1].date.getFormattedTime(location, context, context.is12Hour)
            minutelyContainer.contentDescription =
                activity.getString(
                    R.string.precipitation_between_time,
                    minutelyList[0].date.getFormattedTime(location, context, context.is12Hour),
                    minutelyList[size - 1].date.getFormattedTime(location, context, context.is12Hour)
                )
        } else {
            minutelyContainer.visibility = View.GONE
        }

        minutelyTitle.setTextColor(colors[0])
        minutelyTitle.text = weather.getMinutelyTitle(context)
        minutelySubtitle.text = weather.getMinutelyDescription(context, location)

        precipitationBar.precipitationColor = ThemeManager
            .getInstance(context)
            .weatherThemeDelegate
            .getThemeColors(
                context,
                WeatherViewController.getWeatherKind(location),
                WeatherViewController.isDaylight(location)
            )[0]
        precipitationBar.subLineColor = MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOutline)
        precipitationBar.highlightColor = MainThemeColorProvider.getColor(location, androidx.appcompat.R.attr.colorPrimary)
        precipitationBar.textColor = MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOnPrimary)
        precipitationBar.setShadowColors(colors[0], colors[1], MainThemeColorProvider.isLightTheme(itemView.context, location))

        minutelyStartText.setTextColor(MainThemeColorProvider.getColor(location, R.attr.colorBodyText))
        minutelyCenterText.setTextColor(MainThemeColorProvider.getColor(location, R.attr.colorBodyText))
        minutelyEndText.setTextColor(MainThemeColorProvider.getColor(location, R.attr.colorBodyText))

        minutelyStartLine.setBackgroundColor(MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOutline))
        minutelyEndLine.setBackgroundColor(MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOutline))
    }
}
