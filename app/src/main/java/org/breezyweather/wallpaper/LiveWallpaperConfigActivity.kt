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

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.delay
import org.breezyweather.R
import org.breezyweather.common.activities.BreezyActivity
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.ui.common.widgets.Material3Scaffold
import org.breezyweather.ui.common.widgets.insets.FitStatusBarTopAppBar
import org.breezyweather.ui.main.MainActivity
import org.breezyweather.ui.settings.preference.composables.SwitchPreferenceView
import org.breezyweather.ui.theme.compose.BreezyWeatherTheme
import org.breezyweather.ui.theme.compose.themeRipple
import org.breezyweather.unit.formatting.format

class LiveWallpaperConfigActivity : BreezyActivity() {

    private lateinit var weatherKindValueNow: MutableState<String>
    private lateinit var weatherKinds: Array<String>
    private lateinit var weatherKindValues: Array<String>

    private lateinit var dayNightTypeValueNow: MutableState<String>
    private lateinit var dayNightTypeKinds: Array<String>
    private lateinit var dayNightTypeValues: Array<String>

    private lateinit var animationsEnabledValue: MutableState<Boolean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val liveWallpaperConfigManager = LiveWallpaperConfigManager(this)
        weatherKindValueNow = mutableStateOf(liveWallpaperConfigManager.weatherKind)
        weatherKinds = resources.getStringArray(R.array.live_wallpaper_weather_kinds)
        weatherKindValues = resources.getStringArray(R.array.live_wallpaper_weather_kind_values)

        dayNightTypeValueNow = mutableStateOf(liveWallpaperConfigManager.dayNightType)
        dayNightTypeKinds = resources.getStringArray(R.array.live_wallpaper_day_night_types)
        dayNightTypeValues = resources.getStringArray(R.array.live_wallpaper_day_night_type_values)

        animationsEnabledValue = mutableStateOf(liveWallpaperConfigManager.animationsEnabled)

        setContent {
            BreezyWeatherTheme {
                ContentView()
            }
        }
    }

    @Composable
    private fun ContentView() {
        val context = LocalContext.current
        val dialogOpenState = remember { mutableStateOf(false) }
        Material3Scaffold(
            topBar = {
                FitStatusBarTopAppBar(
                    title = stringResource(R.string.settings_modules_live_wallpaper_title),
                    onBackPressed = { finish() }
                )
            }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxHeight(),
                contentPadding = it
            ) {
                item {
                    Spinner(
                        currentVal = weatherKindValueNow,
                        names = weatherKinds,
                        values = weatherKindValues,
                        titleId = R.string.widget_live_wallpaper_weather_kind
                    )
                }
                item {
                    Spinner(
                        currentVal = dayNightTypeValueNow,
                        names = dayNightTypeKinds,
                        values = dayNightTypeValues,
                        titleId = R.string.widget_live_wallpaper_day_night_type
                    )
                }
                item {
                    SwitchPreferenceView(
                        title = context.getString(
                            R.string.parenthesis,
                            context.getString(R.string.settings_main_section_animations),
                            context.getString(R.string.widget_live_wallpaper_animations_enable_dangerous)
                        ),
                        summary = { _: Context, enabled: Boolean ->
                            if (enabled) {
                                "⚠️ ${context.getString(R.string.settings_enabled)}"
                            } else {
                                context.getString(R.string.settings_disabled)
                            }
                        },
                        checked = animationsEnabledValue.value,
                        withState = false,
                        card = false
                    ) { newValue ->
                        if (newValue) {
                            dialogOpenState.value = true
                        } else {
                            animationsEnabledValue.value = false
                        }
                    }
                }
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(R.dimen.normal_margin)),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Button(
                            onClick = {
                                LiveWallpaperConfigManager.update(
                                    this@LiveWallpaperConfigActivity,
                                    weatherKindValueNow.value,
                                    dayNightTypeValueNow.value,
                                    animationsEnabledValue.value
                                )
                                finish()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.action_save),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
            if (dialogOpenState.value) {
                var timeLeft by remember { mutableIntStateOf(10) }
                LaunchedEffect(key1 = timeLeft) {
                    while (timeLeft > 0) {
                        delay(1000L)
                        --timeLeft
                    }
                }
                AlertDialog(
                    onDismissRequest = {
                        dialogOpenState.value = false
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                dialogOpenState.value = false
                            }
                        ) {
                            Text(stringResource(R.string.action_close))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                animationsEnabledValue.value = true
                                dialogOpenState.value = false
                            },
                            enabled = timeLeft == 0
                        ) {
                            Text(
                                text = if (timeLeft > 0) {
                                    stringResource(
                                        R.string.parenthesis,
                                        stringResource(R.string.action_enable),
                                        timeLeft.format(decimals = 0, locale = context.currentLocale)
                                    )
                                } else {
                                    stringResource(R.string.action_enable)
                                },
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    title = {
                        Text(
                            stringResource(
                                R.string.parenthesis,
                                context.getString(R.string.settings_main_section_animations),
                                stringResource(R.string.widget_live_wallpaper_animations_enable_dangerous)
                            )
                        )
                    },
                    text = {
                        Column {
                            Text(
                                stringResource(R.string.widget_live_wallpaper_animations_enable_warning1)
                            )
                            Spacer(
                                modifier = Modifier.height(dimensionResource(R.dimen.normal_margin))
                            )
                            Text(
                                stringResource(R.string.widget_live_wallpaper_animations_enable_warning2)
                            )
                        }
                    },
                    textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    iconContentColor = MaterialTheme.colorScheme.onSurface
                )
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
                        onClick = { expanded.value = !expanded.value }
                    ),
                label = {
                    Text(
                        text = label,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.clickable {
                            expanded.value = !expanded.value
                        },
                        tint = MaterialTheme.colorScheme.secondary
                    )
                },
                readOnly = true,
                enabled = false,
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            DropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false },
                modifier = Modifier
                    .width(with(LocalDensity.current) { textFieldSize.value.width.toDp() })
            ) {
                names.forEachIndexed { index, item ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = item,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleMedium
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
