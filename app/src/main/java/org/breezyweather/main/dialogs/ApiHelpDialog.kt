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
import androidx.annotation.StringRes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import org.breezyweather.R
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.main.MainActivity
import org.breezyweather.main.utils.MainThemeColorProvider
import org.breezyweather.theme.compose.BreezyWeatherTheme
import org.breezyweather.theme.compose.DayNightTheme

object ApiHelpDialog {
    fun show(
        activity: Activity,
        @StringRes title: Int,
        @StringRes content: Int,
    ) {
        val view = LayoutInflater
            .from(activity)
            .inflate(R.layout.dialog_api_help, activity.findViewById(android.R.id.content), true)

        val composeView = view.findViewById<ComposeView>(R.id.api_help_dialog)
        val dialogOpenState = mutableStateOf(true)
        val isDaylight = if (activity is MainActivity &&
            (!activity.isManagementFragmentVisible || activity.isDrawerLayoutVisible)
        ) { // only use the location-based daylight setting when the home fragment is visible
            activity.isDaylight
        } else {
            null
        }

        composeView.setContent {
            BreezyWeatherTheme(
                MainThemeColorProvider.isLightTheme(activity, daylight = isDaylight)
            ) {
                if (dialogOpenState.value) {
                    AlertDialog(
                        onDismissRequest = {
                            dialogOpenState.value = false
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    IntentHelper.startWeatherProviderSettingsActivity(activity)
                                    dialogOpenState.value = false
                                }
                            ) {
                                Text(stringResource(id = R.string.action_settings))
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    dialogOpenState.value = false
                                }
                            ) {
                                Text(stringResource(id = android.R.string.cancel))
                            }
                        },
                        title = {
                            Text(
                                stringResource(title)
                            )
                        },
                        text = {
                            Text(
                                stringResource(content)
                            )
                        },
                        textContentColor = DayNightTheme.colors.bodyColor,
                        iconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
