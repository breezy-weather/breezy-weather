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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.unit.DistanceUnit
import org.breezyweather.common.basic.models.options.unit.PrecipitationUnit
import org.breezyweather.common.basic.models.options.unit.PressureUnit
import org.breezyweather.common.basic.models.options.unit.SpeedUnit
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.common.widgets.Material3Scaffold
import org.breezyweather.ui.common.widgets.generateCollapsedScrollBehavior
import org.breezyweather.ui.common.widgets.insets.FitStatusBarTopAppBar
import org.breezyweather.ui.settings.preference.composables.ListPreferenceView
import org.breezyweather.ui.settings.preference.composables.PreferenceScreen
import org.breezyweather.ui.settings.preference.listPreferenceItem

@Composable
fun UnitSettingsScreen(
    context: Context,
    onNavigateBack: () -> Unit,
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
        PreferenceScreen(paddingValues = paddings) {
            listPreferenceItem(R.string.settings_units_temperature) { id ->
                ListPreferenceView(
                    titleId = id,
                    selectedKey = SettingsManager.getInstance(context).temperatureUnit.id,
                    valueArrayId = R.array.temperature_unit_values,
                    nameArrayId = R.array.temperature_units,
                    card = true,
                    onValueChanged = {
                        SettingsManager
                            .getInstance(context)
                            .temperatureUnit = TemperatureUnit.getInstance(it)
                    }
                )
            }
            listPreferenceItem(R.string.settings_units_precipitation) { id ->
                ListPreferenceView(
                    titleId = id,
                    selectedKey = SettingsManager.getInstance(context).precipitationUnit.id,
                    valueArrayId = R.array.precipitation_unit_values,
                    nameArrayId = R.array.precipitation_units,
                    card = true,
                    onValueChanged = {
                        SettingsManager
                            .getInstance(context)
                            .precipitationUnit = PrecipitationUnit.getInstance(it)
                    }
                )
            }
            listPreferenceItem(R.string.settings_units_distance) { id ->
                ListPreferenceView(
                    titleId = id,
                    selectedKey = SettingsManager.getInstance(context).distanceUnit.id,
                    valueArrayId = R.array.distance_unit_values,
                    nameArrayId = R.array.distance_units,
                    card = true,
                    onValueChanged = {
                        SettingsManager
                            .getInstance(context)
                            .distanceUnit = DistanceUnit.getInstance(it)
                    }
                )
            }
            listPreferenceItem(R.string.settings_units_speed) { id ->
                ListPreferenceView(
                    titleId = id,
                    selectedKey = SettingsManager.getInstance(context).speedUnit.id,
                    valueArrayId = R.array.speed_unit_values,
                    nameArrayId = R.array.speed_units,
                    card = true,
                    onValueChanged = {
                        SettingsManager
                            .getInstance(context)
                            .speedUnit = SpeedUnit.getInstance(it)
                    }
                )
            }
            listPreferenceItem(R.string.settings_units_pressure) { id ->
                ListPreferenceView(
                    titleId = id,
                    selectedKey = SettingsManager.getInstance(context).pressureUnit.id,
                    valueArrayId = R.array.pressure_unit_values,
                    nameArrayId = R.array.pressure_units,
                    card = true,
                    onValueChanged = {
                        SettingsManager
                            .getInstance(context)
                            .pressureUnit = PressureUnit.getInstance(it)
                    }
                )
            }
        }
    }
}
