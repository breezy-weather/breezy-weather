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

package org.breezyweather.wallpaper

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.toSize
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.ui.widgets.Material3Scaffold
import org.breezyweather.common.ui.widgets.insets.FitStatusBarTopAppBar
import org.breezyweather.theme.compose.BreezyWeatherTheme
import org.breezyweather.theme.compose.DayNightTheme
import org.breezyweather.theme.compose.themeRipple

class LiveWallpaperConfigActivity : GeoActivity() {

    private lateinit var weatherKindValueNow: MutableState<String>
    private lateinit var weatherKinds: Array<String>
    private lateinit var weatherKindValues: Array<String>

    private lateinit var dayNightTypeValueNow: MutableState<String>
    private lateinit var dayNightTypeKinds: Array<String>
    private lateinit var dayNightTypeValues: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val liveWallpaperConfigManager = LiveWallpaperConfigManager(this)
        weatherKindValueNow = mutableStateOf(liveWallpaperConfigManager.weatherKind)
        weatherKinds = resources.getStringArray(R.array.live_wallpaper_weather_kinds)
        weatherKindValues = resources.getStringArray(R.array.live_wallpaper_weather_kind_values)

        dayNightTypeValueNow = mutableStateOf(liveWallpaperConfigManager.dayNightType)
        dayNightTypeKinds = resources.getStringArray(R.array.live_wallpaper_day_night_types)
        dayNightTypeValues = resources.getStringArray(R.array.live_wallpaper_day_night_type_values)

        setContent {
            BreezyWeatherTheme(lightTheme = !isSystemInDarkTheme()) {
                ContentView()
            }
        }
    }

    @Composable
    private fun ContentView() {
        Material3Scaffold(
            topBar = {
                FitStatusBarTopAppBar(
                    title = stringResource(R.string.settings_widgets_live_wallpaper_title),
                    onBackPressed = { finish() },
                )
            },
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxHeight(),
                contentPadding = it,
            ) {
                items(1) {
                    Spinner(
                        currentVal = weatherKindValueNow,
                        names = weatherKinds,
                        values = weatherKindValues,
                        titleId = R.string.widget_live_wallpaper_weather_kind,
                    )
                    Spinner(
                        currentVal = dayNightTypeValueNow,
                        names = dayNightTypeKinds,
                        values = dayNightTypeValues,
                        titleId = R.string.widget_live_wallpaper_day_night_type,
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(R.dimen.normal_margin)),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        Button(
                            onClick = {
                                LiveWallpaperConfigManager.update(
                                    this@LiveWallpaperConfigActivity,
                                    weatherKindValueNow.value,
                                    dayNightTypeValueNow.value,
                                )
                                finish()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        ) {
                            Text(
                                text = stringResource(R.string.action_done),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun Spinner(
        currentVal: MutableState<String>,
        names: Array<String>,
        values: Array<String>,
        @StringRes titleId: Int,
    ) {
        val expanded = remember { mutableStateOf(false) }
        val textFieldSize = remember { mutableStateOf(Size.Zero) }

        val icon = if (expanded.value) {
            Icons.Filled.ArrowDropUp
        } else {
            Icons.Filled.ArrowDropDown
        }
        val label = stringResource(titleId)

        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.normal_margin))
        ) {
            OutlinedTextField(
                value = names[if (values.indexOf(currentVal.value) != -1) values.indexOf(currentVal.value) else 0],
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        textFieldSize.value = coordinates.size.toSize()
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = themeRipple(),
                        onClick = { expanded.value = !expanded.value },
                    ),
                label = {
                    Text(
                        text = label,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold,
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.clickable {
                            expanded.value = !expanded.value
                        },
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                },
                readOnly = true,
                enabled = false,
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
            )
            DropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false },
                modifier = Modifier
                    .width(with(LocalDensity.current){ textFieldSize.value.width.toDp() })
            ) {
                names.forEachIndexed { index, item ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = item,
                                color = DayNightTheme.colors.titleColor,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        },
                        onClick = {
                            currentVal.value = values[index]
                            expanded.value = false
                        }
                    )
                }
            }
        }
    }
}
