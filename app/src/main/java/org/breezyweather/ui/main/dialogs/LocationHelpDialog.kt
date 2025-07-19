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
import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import org.breezyweather.R
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.ui.main.MainActivity
import org.breezyweather.ui.main.utils.MainThemeColorProvider
import org.breezyweather.ui.theme.compose.BreezyWeatherTheme

object LocationHelpDialog {
    fun show(
        activity: Activity,
    ) {
        val view = LayoutInflater
            .from(activity)
            .inflate(R.layout.dialog_location_help, activity.findViewById(android.R.id.content), true)

        val composeView = view.findViewById<ComposeView>(R.id.location_help_dialog)
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
                !MainThemeColorProvider.isLightTheme(activity, daylight = isDaylight)
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
                        title = {
                            Text(
                                stringResource(R.string.location_message_failed_to_locate)
                            )
                        },
                        text = {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                DialogListItem(
                                    content = stringResource(
                                        R.string.location_dialog_failed_to_locate_action_select_source
                                    ),
                                    iconId = R.drawable.ic_factory,
                                    onClick = {
                                        IntentHelper.startLocationProviderSettingsActivity(activity)
                                        dialogOpenState.value = false
                                    }
                                )
                                DialogListItem(
                                    content = stringResource(
                                        R.string.location_dialog_failed_to_locate_action_add_manually,
                                        stringResource(R.string.location_current)
                                    ),
                                    iconId = R.drawable.ic_list,
                                    onClick = {
                                        if (activity is MainActivity) {
                                            activity.setManagementFragmentVisibility(true)
                                        } else {
                                            IntentHelper.startMainActivityForManagement(activity)
                                        }
                                        dialogOpenState.value = false
                                    }
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun DialogListItem(
        content: String,
        @DrawableRes iconId: Int? = null,
        onClick: () -> Unit,
    ) {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = AlertDialogDefaults.containerColor),
            modifier = Modifier.clickable { onClick() },
            headlineContent = {
                Text(
                    content,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingContent = iconId?.let {
                {
                    Icon(
                        painterResource(it),
                        contentDescription = null
                    )
                }
            }
        )
    }
}
