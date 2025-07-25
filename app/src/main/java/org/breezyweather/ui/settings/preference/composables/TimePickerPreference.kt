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

package org.breezyweather.ui.settings.preference.composables

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isContainer
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import org.breezyweather.R
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.ui.common.widgets.Material3ExpressiveCardListItem
import org.breezyweather.ui.common.widgets.defaultCardListItemElevation
import org.breezyweather.ui.theme.compose.themeRipple
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun TimePickerPreferenceView(
    @StringRes titleId: Int,
    currentTime: String,
    enabled: Boolean = true,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    onValueChanged: (String) -> Unit,
) = TimePickerPreferenceView(
    title = stringResource(titleId),
    currentTime = currentTime,
    enabled = enabled,
    isFirst = isFirst,
    isLast = isLast,
    onValueChanged = onValueChanged
)

@Composable
private fun TimePickerPreferenceView(
    title: String,
    currentTime: String,
    enabled: Boolean = true,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    onValueChanged: (String) -> Unit,
) {
    var showTimePicker by remember { mutableStateOf(false) }
    val currentTimeState = remember { mutableStateOf(currentTime) }
    val showingPicker = remember { mutableStateOf(true) }
    val configuration = LocalConfiguration.current
    val is12Hour = LocalContext.current.is12Hour
    val time = SimpleDateFormat("HH:mm", Locale.ENGLISH).parse(currentTimeState.value)

    Material3ExpressiveCardListItem(
        elevation = if (enabled) defaultCardListItemElevation else 0.dp,
        isFirst = isFirst,
        isLast = isLast
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (enabled) 1f else 0.5f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = themeRipple(),
                    onClick = { showTimePicker = true },
                    enabled = enabled
                )
                .padding(dimensionResource(R.dimen.normal_margin)),
            verticalArrangement = Arrangement.Center
        ) {
            Column {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium
                )
                val currentSummary = time?.getFormattedTime(null, LocalContext.current, is12Hour)
                if (currentSummary?.isNotEmpty() == true) {
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.small_margin)))
                    Text(
                        text = currentSummary,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    if (showTimePicker) {
        val cal = Calendar.getInstance()
        time?.let { cal.setTime(it) }

        val timePickerState = rememberTimePickerState(
            initialHour = cal[Calendar.HOUR_OF_DAY],
            initialMinute = cal[Calendar.MINUTE],
            is24Hour = !is12Hour
        )

        TimePickerDialog(
            title = if (showingPicker.value) {
                stringResource(R.string.dialog_time_picker_select_time)
            } else {
                stringResource(R.string.dialog_time_picker_input_time)
            },
            onCancel = { showTimePicker = false },
            onConfirm = {
                currentTimeState.value = timeToString(
                    hour = timePickerState.hour,
                    minute = timePickerState.minute
                )
                showTimePicker = false
                onValueChanged(currentTimeState.value)
            },
            toggle = {
                if (configuration.screenHeightDp > 400) {
                    // Make this take the entire viewport. This will guarantee that Screen readers
                    // focus the toggle first.
                    Box(
                        Modifier
                            .fillMaxSize()
                            .semantics {
                                @Suppress("DEPRECATION")
                                isContainer = true
                            }
                    ) {
                        IconButton(
                            modifier = Modifier
                                // This is a workaround so that the Icon comes up first
                                // in the talkback traversal order. So that users of a11y
                                // services can use the text input. When talkback traversal
                                // order is customizable we can remove this.
                                .size(64.dp, 72.dp)
                                .align(Alignment.BottomStart)
                                .zIndex(5f),
                            onClick = { showingPicker.value = !showingPicker.value }
                        ) {
                            val icon = if (showingPicker.value) {
                                Icons.Outlined.Keyboard
                            } else {
                                Icons.Outlined.Schedule
                            }
                            Icon(
                                icon,
                                contentDescription = if (showingPicker.value) {
                                    stringResource(R.string.dialog_time_picker_toggle_text_input_voice)
                                } else {
                                    stringResource(R.string.dialog_time_picker_toggle_touch_input_voice)
                                }
                            )
                        }
                    }
                }
            }
        ) {
            if (showingPicker.value && configuration.screenHeightDp > 400) {
                TimePicker(state = timePickerState)
            } else {
                TimeInput(state = timePickerState)
            }
        }
    }
}

private fun timeToString(
    hour: Int,
    minute: Int,
): String {
    return Calendar.getInstance()
        .also {
            it.set(Calendar.HOUR_OF_DAY, hour)
            it.set(Calendar.MINUTE, minute)
        }
        .time
        .getFormattedTime(null, null, false)
}

// The TimePickerDialog is not provided by compose material 3. So we need to add it manually.
// See https://issuetracker.google.com/issues/288311426 for more details.
// Source for dialog: https://cs.android.com/androidx/platform/tools/dokka-devsite-plugin/+/master:testData/compose/samples/material3/samples/TimePickerSamples.kt;l=230;drc=03ca30d22e6ee3483142f2e4048db459cb5afb79
@Composable
private fun TimePickerDialog(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    title: String = stringResource(R.string.dialog_time_picker_select_time),
    toggle: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min)
                .background(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface
                )
        ) {
            toggle()
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    text = title,
                    style = MaterialTheme.typography.labelMedium
                )
                content()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(Alignment.CenterVertically),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onCancel
                    ) {
                        Text(
                            text = stringResource(android.R.string.cancel),
                            overflow = TextOverflow.Clip,
                            maxLines = 1
                        )
                    }
                    TextButton(
                        onClick = onConfirm
                    ) {
                        Text(
                            text = stringResource(R.string.action_confirm),
                            overflow = TextOverflow.Clip,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
