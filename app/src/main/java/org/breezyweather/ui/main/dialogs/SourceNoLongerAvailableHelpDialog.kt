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
import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.StringRes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat.getString
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.ui.main.MainActivity
import org.breezyweather.ui.theme.ThemeManager
import org.breezyweather.ui.theme.compose.BreezyWeatherTheme

object SourceNoLongerAvailableHelpDialog {
    fun show(
        activity: Activity,
        @StringRes title: Int,
    ) {
        val view = LayoutInflater
            .from(activity)
            .inflate(R.layout.dialog_source_no_longer_available_help, activity.findViewById(android.R.id.content), true)

        val composeView = view.findViewById<ComposeView>(R.id.source_help_dialog)
        val dialogOpenState = mutableStateOf(true)
        val isDaylight = if (activity is MainActivity &&
            (!activity.isManagementFragmentVisible || activity.isDrawerLayoutVisible)
        ) { // only use the location-based daylight setting when the home fragment is visible
            activity.isDaylight
        } else {
            null
        }
        val helpText = buildHelpText(view.context)

        composeView.setContent {
            BreezyWeatherTheme(
                !ThemeManager.isLightTheme(activity, daylight = isDaylight)
            ) {
                if (dialogOpenState.value) {
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
                        dismissButton = if (activity is MainActivity) {
                            {
                                TextButton(
                                    onClick = {
                                        activity.onEditIconClicked()
                                        dialogOpenState.value = false
                                    }
                                ) {
                                    Text(stringResource(id = R.string.action_change))
                                }
                            }
                        } else {
                            null
                        },
                        title = {
                            Text(
                                stringResource(title)
                            )
                        },
                        text = {
                            Text(
                                helpText
                            )
                        },
                        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        iconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    private fun buildHelpText(context: Context): String {
        return buildString {
            @Suppress("KotlinConstantConditions")
            if (BuildConfig.FLAVOR == "freenet") {
                append(getString(context, R.string.message_source_not_installed_error_content_tip_1))
                append("\n\n")
            }
            append(getString(context, R.string.message_source_not_installed_error_content_tip_2))
        }
    }
}
