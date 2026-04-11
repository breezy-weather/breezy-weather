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

package org.breezyweather.ui.main.dialogs

import android.app.Activity
import android.view.LayoutInflater
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.style.TextAlign
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.ui.common.composables.AlertDialogNoPadding
import org.breezyweather.ui.main.MainActivity
import org.breezyweather.ui.theme.ThemeManager
import org.breezyweather.ui.theme.compose.BreezyWeatherTheme

object LicenseComplianceDialog {
    fun show(
        activity: Activity,
    ) {
        val view = LayoutInflater
            .from(activity)
            .inflate(R.layout.dialog_license_compliance, activity.findViewById(android.R.id.content), true)

        val composeView = view.findViewById<ComposeView>(R.id.license_compliance_dialog)
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
                !ThemeManager.isLightTheme(activity, daylight = isDaylight)
            ) {
                if (dialogOpenState.value) {
                    AlertDialogNoPadding(
                        onDismissRequest = {
                            activity.finish()
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    activity.finish()
                                }
                            ) {
                                Text(
                                    "I understand, I will make the changes",
                                    textAlign = TextAlign.Center
                                )
                            }
                        },
                        title = {
                            Text(
                                "License compliance"
                            )
                        },
                        text = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                DialogListItem(
                                    "It looks like you’re running an unofficial release build of Breezy Weather"
                                )
                                DialogListItem(
                                    "If you downloaded Breezy Weather from official sources, and have no idea " +
                                        "what this message means, your device is probably infested"
                                )
                                DialogListItem(
                                    "If you’re making releases for a fork of Breezy Weather, you have the right to " +
                                        "do so, but you need to respect the license terms mentioned in the README file:"
                                )
                                if (BuildConfig.IS_BREEZY) {
                                    DialogListItem(
                                        "Compile without the `breezy` flag, so that your app is distinct " +
                                            "enough from ours",
                                        icon = "❌"
                                    )
                                } else {
                                    DialogListItem(
                                        "Use your own app name, so that your users are not misled into thinking " +
                                            "they are using our app.\nCurrent app name: " +
                                            activity.getString(R.string.brand_name),
                                        icon = if (activity.getString(R.string.brand_name)
                                                .contains("breezy", ignoreCase = true)
                                        ) {
                                            "❌"
                                        } else {
                                            "✅"
                                        }
                                    )
                                }
                                DialogListItem(
                                    "Use your own applicationId, so that users can install Breezy Weather and " +
                                        "your app in parallel.\nCurrent applicationId: " + BuildConfig.APPLICATION_ID,
                                    icon = if (BuildConfig.APPLICATION_ID.contains("breezy", ignoreCase = true)) {
                                        "❌"
                                    } else {
                                        "✅"
                                    }
                                )
                            }
                        },
                        containerColor = Color(activity.getColor(R.color.alert_background)),
                        textContentColor = Color(activity.getColor(R.color.colorTextTitle)),
                        iconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    @Composable
    private fun DialogListItem(
        content: String,
        icon: String? = null,
    ) {
        ListItem(
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            ),
            headlineContent = {
                Text(
                    content,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingContent = icon?.let {
                {
                    Text(it)
                }
            }
        )
    }
}
