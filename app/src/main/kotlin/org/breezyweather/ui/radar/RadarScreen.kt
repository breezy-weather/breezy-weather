/*
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

package org.breezyweather.ui.radar

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.breezyweather.R
import org.breezyweather.common.extensions.setSystemBarStyle
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.common.widgets.Material3Scaffold
import org.breezyweather.ui.common.widgets.insets.BWCenterAlignedTopAppBar
import org.breezyweather.ui.radar.components.RadarTile
import org.breezyweather.ui.radar.components.RadarWebView
import org.breezyweather.ui.theme.ThemeManager
import org.breezyweather.ui.theme.compose.BreezyWeatherTheme

/**
 * TODO:
 * - Add onboarding screen (tile selector, radar source selector)
 * - Add configuration for tiles (change URL)
 * - Save configuration (opacity (global), color scheme (source-specific), preferred type radar/sattelite (source-specific))
 */
@Composable
internal fun RadarScreen(
    onBackPressed: () -> Unit,
    radarViewModel: RadarViewModel = viewModel(),
) {
    val radarUiState by radarViewModel.uiState.collectAsState()

    val dialogSettingsOpenState = remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    val activity = LocalActivity.current
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val isLightTheme = ThemeManager.isLightTheme(context, radarUiState.location)

    LaunchedEffect(radarUiState.location) {
        // re-setting the status bar color once the location is fetched
        if (radarUiState.location != null && activity != null) {
            activity.window.setSystemBarStyle(isLightTheme)
            radarViewModel.loadRadarInfos(context)
        }
    }

    BreezyWeatherTheme(!isLightTheme) {
        Material3Scaffold(
            topBar = {
                BWCenterAlignedTopAppBar(
                    title = stringResource(R.string.radar_title),
                    onBackPressed = onBackPressed,
                    actions = {
                        // TODO: Icon to switch source

                        Box(
                            modifier = Modifier
                                .padding(16.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    expanded = !expanded
                                }
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_factory),
                                    contentDescription = stringResource(R.string.action_change), // TODO: Change source
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                radarUiState.sourceList?.forEach {
                                    // TODO: Highlight selected
                                    DropdownMenuItem(
                                        text = {
                                            Text(it.name)
                                        },
                                        onClick = {
                                            SettingsManager.getInstance(context).radarSource = it.id
                                            radarViewModel.viewModelScope.launch {
                                                radarViewModel.loadRadarInfos(context)
                                            }
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        if (radarUiState.wrapper != null) {
                            IconButton(
                                onClick = {
                                    dialogSettingsOpenState.value = true
                                }
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_settings),
                                    contentDescription = stringResource(R.string.action_settings),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        } else if (radarUiState.webViewUrl != null) {
                            IconButton(
                                onClick = {
                                    uriHandler.openUri(radarUiState.webViewUrl!!)
                                }
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_open_in_new),
                                    contentDescription = stringResource(R.string.action_open_in_external_browser),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                )
            }
        ) {
            if (radarUiState.wrapper != null) {
                RadarTile(
                    radarUiState = radarUiState,
                    dialogSettingsOpenState = dialogSettingsOpenState.value,
                    closeSettings = {
                        dialogSettingsOpenState.value = false
                    },
                    modifier = Modifier.padding(it)
                )
            } else if (radarUiState.webViewUrl != null) {
                RadarWebView(
                    radarUiState = radarUiState,
                    modifier = Modifier.padding(it)
                )
            }
        }
    }
}

internal const val INITIAL_ZOOM_LEVEL = 10
