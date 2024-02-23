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

package org.breezyweather.main.dialogs

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.breezyweather.R
import breezyweather.domain.location.model.Location
import org.breezyweather.common.basic.models.options.unit.ProbabilityUnit
import breezyweather.domain.weather.model.Hourly
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.ui.widgets.AnimatableIconView
import org.breezyweather.domain.weather.model.getHour
import org.breezyweather.settings.SettingsManager
import org.breezyweather.theme.resource.ResourceHelper
import org.breezyweather.theme.resource.ResourcesProviderFactory

object HourlyWeatherDialog {
    fun show(activity: Activity, location: Location, hourly: Hourly) {
        val view = LayoutInflater
            .from(activity)
            .inflate(R.layout.dialog_weather_hourly, null, false)
        initWidget(view, hourly)
        MaterialAlertDialogBuilder(activity)
            .setTitle(
                hourly.getHour(activity, location.timeZone)
                        + " - "
                        + hourly.date.getFormattedDate(location.timeZone, activity.getString(R.string.date_format_long))
            )
            .setView(view)
            .show()
    }

    private fun initWidget(view: View, hourly: Hourly) {
        val provider = ResourcesProviderFactory.newInstance
        val weatherIcon = view.findViewById<AnimatableIconView>(R.id.dialog_weather_hourly_icon)
        view.findViewById<View>(R.id.dialog_weather_hourly_weatherContainer)
            .setOnClickListener { weatherIcon.startAnimators() }
        val weatherCode = hourly.weatherCode
        val daytime = hourly.isDaylight
        if (weatherCode != null) {
            weatherIcon.setAnimatableIcon(
                ResourceHelper.getWeatherIcons(provider, weatherCode, daytime),
                ResourceHelper.getWeatherAnimators(provider, weatherCode, daytime)
            )
        }
        val weatherText = view.findViewById<TextView>(R.id.dialog_weather_hourly_text)
        val settings = SettingsManager.getInstance(view.context)
        val temperatureUnit = settings.temperatureUnit
        val precipitationUnit = settings.precipitationUnit
        val builder = StringBuilder()
        hourly.weatherText?.let {
            builder.append(it)
        }
        hourly.temperature?.temperature?.let {
            if (builder.toString().isNotEmpty()) builder.append(", ")
            builder.append(
                temperatureUnit.getValueText(view.context, it)
            )
        }
        hourly.temperature?.feelsLikeTemperature?.let {
            if (builder.toString().isNotEmpty()) builder.append("\n")
            builder.append(view.context.getString(R.string.temperature_feels_like))
                .append(" ")
                .append(
                    temperatureUnit.getValueText(view.context, it)
                )
        }
        hourly.precipitation?.total?.let {
            if (builder.toString().isNotEmpty()) builder.append("\n")
            builder.append(view.context.getString(R.string.precipitation))
                .append(view.context.getString(R.string.colon_separator))
                .append(precipitationUnit.getValueText(view.context, it))
        }
        if ((hourly.precipitationProbability?.total ?: 0.0) > 0) {
            if (builder.toString().isNotEmpty()) builder.append("\n")
            builder.append(view.context.getString(R.string.precipitation_probability))
                .append(view.context.getString(R.string.colon_separator))
                .append(ProbabilityUnit.PERCENT.getValueText(view.context, hourly.precipitationProbability!!.total!!.toInt()))
        }
        weatherText.text = builder.toString()
    }
}
