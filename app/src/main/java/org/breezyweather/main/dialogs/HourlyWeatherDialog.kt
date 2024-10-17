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
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Hourly
import org.breezyweather.R
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getFormattedMediumDayAndMonth
import org.breezyweather.common.extensions.getHour
import org.breezyweather.common.ui.widgets.AnimatableIconView
import org.breezyweather.domain.location.model.isDaylight
import org.breezyweather.main.utils.MainThemeColorProvider
import org.breezyweather.settings.SettingsManager
import org.breezyweather.theme.compose.BreezyWeatherTheme
import org.breezyweather.theme.resource.ResourceHelper
import org.breezyweather.theme.resource.ResourcesProviderFactory
import java.text.NumberFormat

object HourlyWeatherDialog {
    fun show(
        activity: Activity,
        location: Location,
        hourly: Hourly
    ) {
        val view = LayoutInflater
            .from(activity)
            .inflate(R.layout.dialog_weather_hourly, activity.findViewById(android.R.id.content), true)

        val composeView = view.findViewById<ComposeView>(R.id.dialog_weather_hourly)
        val weatherIconView = AnimatableIconView(activity.baseContext)

        val weatherText = getWeatherText(view, hourly)
        //val weatherIcon = getWeatherIcon(hourly)
        val dialogOpenState = mutableStateOf(true)

        composeView.setContent {
            BreezyWeatherTheme(lightTheme = MainThemeColorProvider.isLightTheme(
                activity, daylight = location.isDaylight
            )) {
                if (dialogOpenState.value) {
                    AlertDialog(
                        onDismissRequest = {
                            dialogOpenState.value = false
                        },
                        confirmButton = {},
                        dismissButton = {},
                        icon = {
                            AndroidView(
                                modifier = Modifier.size(48.dp),
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
                                        setOnClickListener { startAnimators() }
                                    }
                                }
                            )
                            /*
                            weatherIcon?.toBitmapOrNull()?.asImageBitmap()?.let {
                                Icon(
                                    bitmap = it,
                                    tint = Color.Unspecified,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp)
                                )
                            }*/
                        },
                        title = { Text(hourly.date.getHour(location, activity)
                                + " - "
                                + hourly.date.getFormattedMediumDayAndMonth(location, activity)) },
                        text = {
                            Text(
                                text = weatherText,
                                modifier = Modifier.clickable {
                                    weatherIconView.startAnimators()
                                }
                            )
                        }
                    )
                }
            }
        }
    }

    private fun getWeatherText(view: View, hourly: Hourly): String {
        val settings = SettingsManager.getInstance(view.context)
        val temperatureUnit = settings.temperatureUnit
        val precipitationUnit = settings.precipitationUnit
        val builder = StringBuilder()
        hourly.weatherText?.let {
            builder.append(it)
        }
        hourly.temperature?.temperature?.let {
            if (builder.toString().isNotEmpty()) {
                builder.append(view.context.getString(R.string.comma_separator))
            }
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
                .append(
                    NumberFormat.getPercentInstance(view.context.currentLocale).apply {
                        maximumFractionDigits = 0
                    }.format(hourly.precipitationProbability!!.total!!.div(100.0))
                )
        }
        return builder.toString()
    }

    private fun getWeatherIcon(hourly: Hourly): Drawable? {
        val provider = ResourcesProviderFactory.newInstance

        return hourly.weatherCode?.let {
            ResourceHelper.getWeatherIcon(provider,
                it, hourly.isDaylight)
        }
    }
}
