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

package org.breezyweather.ui.settings.compose

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.unit.DistanceUnit
import org.breezyweather.common.basic.models.options.unit.PrecipitationUnit
import org.breezyweather.common.basic.models.options.unit.PressureUnit
import org.breezyweather.common.basic.models.options.unit.SpeedUnit
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.extensions.plus
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.common.widgets.Material3Scaffold
import org.breezyweather.ui.common.widgets.generateCollapsedScrollBehavior
import org.breezyweather.ui.common.widgets.insets.FitStatusBarTopAppBar
import org.breezyweather.ui.settings.preference.composables.ListPreferenceViewWithCard
import org.breezyweather.ui.settings.preference.composables.PreferenceScreen
import org.breezyweather.ui.settings.preference.listPreferenceItem
import org.breezyweather.ui.settings.preference.smallSeparatorItem

@Composable
fun UnitSettingsScreen(
    context: Context,
    onNavigateBack: () -> Unit,
    updateWidgetIfNecessary: (Context) -> Unit,
    updateNotificationIfNecessary: (Context) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = generateCollapsedScrollBehavior()

    Material3Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            FitStatusBarTopAppBar(
                title = stringResource(R.string.settings_units),
                onBackPressed = onNavigateBack,
                actions = { AboutActivityIconButton(context) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddings ->
        PreferenceScreen(
            paddingValues = paddings.plus(PaddingValues(horizontal = dimensionResource(R.dimen.normal_margin)))
        ) {
            listPreferenceItem(R.string.settings_units_temperature) { id ->
                val valueArray = stringArrayResource(R.array.temperature_unit_values)
                val nameArray = stringArrayResource(R.array.temperature_units).mapIndexed { index, value ->
                    if (index == 0) {
                        stringResource(
                            R.string.parenthesis,
                            stringResource(R.string.settings_follow_system),
                            TemperatureUnit.getDefaultUnit(context).getName(context)
                        )
                    } else {
                        value
                    }
                }.toTypedArray()
                ListPreferenceViewWithCard(
                    title = stringResource(id),
                    summary = { _, value -> nameArray[valueArray.indexOfFirst { it == value }] },
                    selectedKey = SettingsManager.getInstance(context).temperatureUnit?.id ?: "auto",
                    valueArray = valueArray,
                    nameArray = nameArray,
                    isFirst = true,
                    onValueChanged = { temperatureUnitId ->
                        SettingsManager.getInstance(context).temperatureUnit = if (temperatureUnitId != "auto") {
                            TemperatureUnit.entries.firstOrNull { it.id == temperatureUnitId }
                        } else {
                            null
                        }

                        // Widgets and notification-widget may use units, update them
                        updateWidgetIfNecessary(context)
                        updateNotificationIfNecessary(context)
                    }
                )
            }
            smallSeparatorItem()
            listPreferenceItem(R.string.settings_units_precipitation) { id ->
                val valueArray = stringArrayResource(R.array.precipitation_unit_values)
                val nameArray = stringArrayResource(R.array.precipitation_units).mapIndexed { index, value ->
                    if (index == 0) {
                        val defaultUnit = PrecipitationUnit.getDefaultUnit(context)
                        val snowfallUnit = PrecipitationUnit.getDefaultSnowfallUnit(context)
                        stringResource(
                            R.string.parenthesis,
                            stringResource(R.string.settings_follow_system),
                            if (defaultUnit != snowfallUnit) {
                                defaultUnit.getName(context) + "/" + snowfallUnit.getName(context)
                            } else {
                                defaultUnit.getName(context)
                            }
                        )
                    } else {
                        value
                    }
                }.toTypedArray()
                ListPreferenceViewWithCard(
                    title = stringResource(id),
                    summary = { _, value -> nameArray[valueArray.indexOfFirst { it == value }] },
                    selectedKey = SettingsManager.getInstance(context).precipitationUnit?.id ?: "auto",
                    valueArray = valueArray,
                    nameArray = nameArray,
                    onValueChanged = { precipitationUnitId ->
                        SettingsManager.getInstance(context).precipitationUnit = if (precipitationUnitId != "auto") {
                            PrecipitationUnit.entries.firstOrNull { it.id == precipitationUnitId }
                        } else {
                            null
                        }

                        // Widgets and notification-widget may use units, update them
                        updateWidgetIfNecessary(context)
                        updateNotificationIfNecessary(context)
                    }
                )
            }
            smallSeparatorItem()
            listPreferenceItem(R.string.settings_units_speed) { id ->
                val valueArray = stringArrayResource(R.array.speed_unit_values)
                val nameArray = stringArrayResource(R.array.speed_units).mapIndexed { index, value ->
                    if (index == 0) {
                        stringResource(
                            R.string.parenthesis,
                            stringResource(R.string.settings_follow_system),
                            SpeedUnit.getDefaultUnit(context).getName(context)
                        )
                    } else {
                        value
                    }
                }.toTypedArray()
                ListPreferenceViewWithCard(
                    title = stringResource(id),
                    summary = { _, value -> nameArray[valueArray.indexOfFirst { it == value }] },
                    selectedKey = SettingsManager.getInstance(context).speedUnit?.id ?: "auto",
                    valueArray = valueArray,
                    nameArray = nameArray,
                    onValueChanged = { speedUnitId ->
                        SettingsManager.getInstance(context).speedUnit = if (speedUnitId != "auto") {
                            SpeedUnit.entries.firstOrNull { it.id == speedUnitId }
                        } else {
                            null
                        }

                        // Widgets and notification-widget may use units, update them
                        updateWidgetIfNecessary(context)
                        updateNotificationIfNecessary(context)
                    }
                )
            }
            smallSeparatorItem()
            listPreferenceItem(R.string.settings_units_distance) { id ->
                val valueArray = stringArrayResource(R.array.distance_unit_values)
                val nameArray = stringArrayResource(R.array.distance_units).mapIndexed { index, value ->
                    if (index == 0) {
                        stringResource(
                            R.string.parenthesis,
                            stringResource(R.string.settings_follow_system),
                            DistanceUnit.getDefaultUnit(context).getName(context)
                        )
                    } else {
                        value
                    }
                }.toTypedArray()
                ListPreferenceViewWithCard(
                    title = stringResource(id),
                    summary = { _, value -> nameArray[valueArray.indexOfFirst { it == value }] },
                    selectedKey = SettingsManager.getInstance(context).distanceUnit?.id ?: "auto",
                    valueArray = valueArray,
                    nameArray = nameArray,
                    onValueChanged = { distanceUnitId ->
                        SettingsManager.getInstance(context).distanceUnit = if (distanceUnitId != "auto") {
                            DistanceUnit.entries.firstOrNull { it.id == distanceUnitId }
                        } else {
                            null
                        }

                        // Widgets and notification-widget may use units, update them
                        updateWidgetIfNecessary(context)
                        updateNotificationIfNecessary(context)
                    }
                )
            }
            smallSeparatorItem()
            listPreferenceItem(R.string.settings_units_pressure) { id ->
                val valueArray = stringArrayResource(R.array.pressure_unit_values)
                val nameArray = stringArrayResource(R.array.pressure_units).mapIndexed { index, value ->
                    if (index == 0) {
                        stringResource(
                            R.string.parenthesis,
                            stringResource(R.string.settings_follow_system),
                            PressureUnit.getDefaultUnit(context).getName(context)
                        )
                    } else {
                        value
                    }
                }.toTypedArray()
                ListPreferenceViewWithCard(
                    title = stringResource(id),
                    summary = { _, value -> nameArray[valueArray.indexOfFirst { it == value }] },
                    selectedKey = SettingsManager.getInstance(context).pressureUnit?.id ?: "auto",
                    valueArray = valueArray,
                    nameArray = nameArray,
                    isLast = true,
                    onValueChanged = { pressureUnitId ->
                        SettingsManager.getInstance(context).pressureUnit = if (pressureUnitId != "auto") {
                            PressureUnit.entries.firstOrNull { it.id == pressureUnitId }
                        } else {
                            null
                        }

                        // Widgets and notification-widget may use units, update them
                        updateWidgetIfNecessary(context)
                        updateNotificationIfNecessary(context)
                    }
                )
            }
        }
    }
}
