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

package org.breezyweather.ui.main.dialogs

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.viewinterop.AndroidView
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Hourly
import org.breezyweather.R
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getFormattedMediumDayAndMonth
import org.breezyweather.common.extensions.getHour
import org.breezyweather.domain.location.model.isDaylight
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.common.widgets.AnimatableIconView
import org.breezyweather.ui.main.utils.MainThemeColorProvider
import org.breezyweather.ui.theme.compose.BreezyWeatherTheme
import org.breezyweather.ui.theme.compose.DayNightTheme
import org.breezyweather.ui.theme.resource.ResourceHelper
import org.breezyweather.ui.theme.resource.ResourcesProviderFactory
import java.text.NumberFormat

object HourlyWeatherDialog {
    fun show(
        activity: Activity,
        location: Location,
        hourly: Hourly,
    ) {
        val view = LayoutInflater
            .from(activity)
            .inflate(R.layout.dialog_weather_hourly, activity.findViewById(android.R.id.content), true)

        val composeView = view.findViewById<ComposeView>(R.id.dialog_weather_hourly)
        val dialogOpenState = mutableStateOf(true)
        val weatherIconView = AnimatableIconView(view.context)
        val weatherText = buildWeatherText(view, hourly)

        composeView.setContent {
            BreezyWeatherTheme(
                MainThemeColorProvider.isLightTheme(activity, daylight = location.isDaylight)
            ) {
                if (dialogOpenState.value) {
                    AlertDialog(
                        onDismissRequest = {
                            dialogOpenState.value = false
                        },
                        confirmButton = { /* do not show a button */ },
                        title = {
                            Text(
                                hourly.date.getHour(location, activity) +
                                    " - " +
                                    hourly.date.getFormattedMediumDayAndMonth(location, activity)
                            )
                        },
                        text = {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        weatherIconView.startAnimators()
                                    }
                            ) {
                                AndroidView(
                                    modifier = Modifier.size(dimensionResource(R.dimen.standard_weather_icon_size)),
                                    factory = {
                                        weatherIconView.apply {
                                            val provider = ResourcesProviderFactory.newInstance
                                            val weatherCode = hourly.weatherCode
                                            val daytime = hourly.isDaylight
                                            if (weatherCode != null) {
                                                setAnimatableIcon(
                                                    ResourceHelper.getWeatherIcons(
                                                        provider,
                                                        weatherCode,
                                                        daytime
                                                    ),
                                                    ResourceHelper.getWeatherAnimators(
                                                        provider,
                                                        weatherCode,
                                                        daytime
                                                    )
                                                )
                                            }
                                        }
                                    }
                                )
                                Text(
                                    text = weatherText,
                                    modifier = Modifier.padding(
                                        start = dimensionResource(R.dimen.normal_margin)
                                    )
                                )
                            }
                        },
                        textContentColor = DayNightTheme.colors.bodyColor
                    )
                }
            }
        }
    }

    private fun buildWeatherText(view: View, hourly: Hourly): String {
        val settings = SettingsManager.getInstance(view.context)
        val temperatureUnit = settings.temperatureUnit
        val precipitationUnit = settings.precipitationUnit

        return buildString {
            append(hourly.weatherText)
            hourly.temperature?.temperature?.let {
                if (isNotEmpty()) append(view.context.getString(R.string.comma_separator))
                append(temperatureUnit.getValueText(view.context, it))
            }
            hourly.temperature?.feelsLikeTemperature?.let {
                if (isNotEmpty()) append("\n")
                append(view.context.getString(R.string.temperature_feels_like))
                append(" ")
                append(temperatureUnit.getValueText(view.context, it))
            }
            hourly.precipitation?.total?.let {
                if (isNotEmpty()) append("\n")
                append(view.context.getString(R.string.precipitation))
                append(view.context.getString(R.string.colon_separator))
                append(precipitationUnit.getValueText(view.context, it))
            }
            if ((hourly.precipitationProbability?.total ?: 0.0) > 0) {
                if (isNotEmpty()) append("\n")
                append(view.context.getString(R.string.precipitation_probability))
                append(view.context.getString(R.string.colon_separator))
                append(
                    NumberFormat.getPercentInstance(view.context.currentLocale).apply {
                        maximumFractionDigits = 0
                    }.format(hourly.precipitationProbability!!.total!!.div(100.0))
                )
            }
        }
    }
}
