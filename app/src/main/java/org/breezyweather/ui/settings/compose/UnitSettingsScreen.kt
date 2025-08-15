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
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.plus
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.common.widgets.Material3Scaffold
import org.breezyweather.ui.common.widgets.generateCollapsedScrollBehavior
import org.breezyweather.ui.common.widgets.insets.FitStatusBarTopAppBar
import org.breezyweather.ui.settings.preference.composables.ListPreferenceViewWithCard
import org.breezyweather.ui.settings.preference.composables.PreferenceScreen
import org.breezyweather.ui.settings.preference.listPreferenceItem
import org.breezyweather.ui.settings.preference.smallSeparatorItem
import org.breezyweather.unit.distance.DistanceUnit
import org.breezyweather.unit.formatting.UnitWidth
import org.breezyweather.unit.precipitation.PrecipitationUnit
import org.breezyweather.unit.pressure.PressureUnit
import org.breezyweather.unit.speed.SpeedUnit

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
                            stringResource(R.string.settings_regional_preference),
                            TemperatureUnit.getDefaultUnit(context).getName(context)
                        )
                    } else {
                        value
                    }
                }.toTypedArray()
                ListPreferenceViewWithCard(
                    title = stringResource(id),
                    summary = { _, value ->
                        valueArray.indexOfFirst { it == value }.let { if (it == -1) nameArray[0] else nameArray[it] }
                    },
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
                val allowedPrecipitationUnits = PrecipitationUnit.entries.filter { it != PrecipitationUnit.MICROMETER }
                val valueArray = arrayOf("auto") + allowedPrecipitationUnits.map { it.id }
                val defaultUnit = PrecipitationUnit.getDefaultUnit(context.currentLocale)
                val snowfallUnit = PrecipitationUnit.getDefaultSnowfallUnit(context.currentLocale)
                val nameArray = arrayOf(
                    stringResource(
                        R.string.parenthesis,
                        stringResource(R.string.settings_regional_preference),
                        if (defaultUnit != snowfallUnit) {
                            defaultUnit.getDisplayName(context, context.currentLocale, UnitWidth.LONG) +
                                "/" +
                                snowfallUnit.getDisplayName(context, context.currentLocale, UnitWidth.LONG)
                        } else {
                            defaultUnit.getDisplayName(context, context.currentLocale, UnitWidth.LONG)
                        }
                    )
                ) + allowedPrecipitationUnits.map {
                    it.getDisplayName(context, context.currentLocale, UnitWidth.LONG)
                }
                ListPreferenceViewWithCard(
                    title = stringResource(id),
                    summary = { _, value ->
                        valueArray.indexOfFirst { it == value }.let { if (it == -1) nameArray[0] else nameArray[it] }
                    },
                    selectedKey = SettingsManager.getInstance(context).precipitationUnit?.id ?: "auto",
                    valueArray = valueArray,
                    nameArray = nameArray,
                    onValueChanged = { precipitationUnitId ->
                        SettingsManager.getInstance(context).precipitationUnit = if (precipitationUnitId != "auto") {
                            PrecipitationUnit.getUnit(precipitationUnitId)
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
                val allowedSpeedUnits = SpeedUnit.entries.filter { it != SpeedUnit.CENTIMETER_PER_SECOND }
                val valueArray = arrayOf("auto") + allowedSpeedUnits.map { it.id }
                val nameArray = arrayOf(
                    stringResource(
                        R.string.parenthesis,
                        stringResource(R.string.settings_regional_preference),
                        SpeedUnit.getDefaultUnit(context.currentLocale)
                            .getDisplayName(context, context.currentLocale, UnitWidth.LONG)
                    )
                ) + allowedSpeedUnits.map {
                    it.getDisplayName(context, context.currentLocale, UnitWidth.LONG)
                }
                ListPreferenceViewWithCard(
                    title = stringResource(id),
                    summary = { _, value ->
                        valueArray.indexOfFirst { it == value }.let { if (it == -1) nameArray[0] else nameArray[it] }
                    },
                    selectedKey = SettingsManager.getInstance(context).speedUnit?.id ?: "auto",
                    valueArray = valueArray,
                    nameArray = nameArray,
                    onValueChanged = { speedUnitId ->
                        SettingsManager.getInstance(context).speedUnit = if (speedUnitId != "auto") {
                            SpeedUnit.getUnit(speedUnitId)
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
                val allowedDistanceUnits = DistanceUnit.entries
                val valueArray = arrayOf("auto") + allowedDistanceUnits.map { it.id }
                val nameArray = arrayOf(
                    stringResource(
                        R.string.parenthesis,
                        stringResource(R.string.settings_regional_preference),
                        DistanceUnit.getDefaultUnit(context.currentLocale)
                            .getDisplayName(context, context.currentLocale, UnitWidth.LONG)
                    )
                ) + allowedDistanceUnits.map {
                    it.getDisplayName(context, context.currentLocale, UnitWidth.LONG)
                }
                ListPreferenceViewWithCard(
                    title = stringResource(id),
                    summary = { _, value ->
                        valueArray.indexOfFirst { it == value }.let { if (it == -1) nameArray[0] else nameArray[it] }
                    },
                    selectedKey = SettingsManager.getInstance(context).distanceUnit?.id ?: "auto",
                    valueArray = valueArray,
                    nameArray = nameArray,
                    onValueChanged = { distanceUnitId ->
                        SettingsManager.getInstance(context).distanceUnit = if (distanceUnitId != "auto") {
                            DistanceUnit.getUnit(distanceUnitId)
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
                val allowedPressureUnits = PressureUnit.entries.filter { it != PressureUnit.PASCAL }
                val valueArray = arrayOf("auto") + allowedPressureUnits.map { it.id }
                val nameArray = arrayOf(
                    stringResource(
                        R.string.parenthesis,
                        stringResource(R.string.settings_regional_preference),
                        PressureUnit.getDefaultUnit(context.currentLocale)
                            .getDisplayName(context, context.currentLocale, UnitWidth.LONG)
                    )
                ) + allowedPressureUnits.map {
                    it.getDisplayName(context, context.currentLocale, UnitWidth.LONG)
                }
                ListPreferenceViewWithCard(
                    title = stringResource(id),
                    summary = { _, value ->
                        valueArray.indexOfFirst { it == value }.let { if (it == -1) nameArray[0] else nameArray[it] }
                    },
                    selectedKey = SettingsManager.getInstance(context).pressureUnit?.id ?: "auto",
                    valueArray = valueArray,
                    nameArray = nameArray,
                    isLast = true,
                    onValueChanged = { pressureUnitId ->
                        SettingsManager.getInstance(context).pressureUnit = if (pressureUnitId != "auto") {
                            PressureUnit.getUnit(pressureUnitId)
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
