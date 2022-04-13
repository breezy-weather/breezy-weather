package wangdaye.com.geometricweather.settings.compose

import android.widget.TimePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.theme.compose.DayNightTheme
import wangdaye.com.geometricweather.theme.compose.GeometricWeatherTheme
import wangdaye.com.geometricweather.theme.compose.rememberThemeRipple

@Composable
fun SectionHeader(
    title: String,
) {
    Box(modifier = Modifier.padding(dimensionResource(R.dimen.normal_margin))) {
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
fun SectionFooter() {
    Divider(color = MaterialTheme.colorScheme.outline)
}

@Composable
fun PreferenceView(
    title: String,
    summary: String? = null,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberThemeRipple(),
                onClick = onClick,
                enabled = enabled,
            )
            .padding(dimensionResource(R.dimen.normal_margin)),
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            color = DayNightTheme.colors.titleColor,
            style = MaterialTheme.typography.titleMedium,
        )
        if (summary?.isNotEmpty() == true) {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.little_margin)))
            Text(
                text = summary,
                color = DayNightTheme.colors.captionColor,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
fun CheckboxPreferenceView(
    title: String,
    summary: @Composable (Boolean) -> String?,
    checked: Boolean,
    enabled: Boolean = true,
    onValueChanged: (Boolean) -> Unit,
) {
    val state = remember { mutableStateOf(checked) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberThemeRipple(),
                onClick = {
                    state.value = !state.value
                    onValueChanged(state.value)
                },
                enabled = enabled,
            )
            .padding(dimensionResource(R.dimen.normal_margin)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = DayNightTheme.colors.titleColor,
                style = MaterialTheme.typography.titleMedium,
            )
            val currentSummary = summary(state.value)
            if (currentSummary?.isNotEmpty() == true) {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.little_margin)))
                Text(
                    text = currentSummary,
                    color = DayNightTheme.colors.captionColor,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.little_margin)))
        Checkbox(
            checked = state.value,
            onCheckedChange = {
                state.value = it
                onValueChanged(it)
            },
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.6f
                ),
                checkmarkColor = MaterialTheme.colorScheme.surface,
                disabledColor = MaterialTheme.colorScheme.onSurface.copy(
                    alpha = ContentAlpha.disabled
                ),
                disabledIndeterminateColor = MaterialTheme.colorScheme.primary.copy(
                    alpha = ContentAlpha.disabled
                )
            )
        )
    }
}

@Composable
fun ListPreferenceView(
    title: String,
    summary: @Composable (String) -> String?, // key -> summary.
    selectedKey: String,
    keyNamePairList: List<Pair<String, String>>, // key, name.
    enabled: Boolean = true,
    onValueChanged: (String) -> Unit,
) {
    val listSelectedState = remember { mutableStateOf(selectedKey) }
    val dialogOpenState = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberThemeRipple(),
                onClick = { dialogOpenState.value = true },
                enabled = enabled,
            )
            .padding(dimensionResource(R.dimen.normal_margin)),
        verticalArrangement = Arrangement.Center,
    ) {
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = DayNightTheme.colors.titleColor,
                style = MaterialTheme.typography.titleMedium,
            )
            val currentSummary = summary(listSelectedState.value)
            if (currentSummary?.isNotEmpty() == true) {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.little_margin)))
                Text(
                    text = currentSummary,
                    color = DayNightTheme.colors.captionColor,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }

    if (dialogOpenState.value) {
        AlertDialog(
            onDismissRequest = { dialogOpenState.value = false },
            title = {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineSmall,
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(keyNamePairList) {
                        RadioButton(
                            selected = listSelectedState.value == it.first,
                            onClick = {
                                listSelectedState.value = it.first
                                dialogOpenState.value = false
                                onValueChanged(it.first)
                            },
                            text = it.second,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { dialogOpenState.value = false }
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        )
    }
}

@Composable
private fun RadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    text: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberThemeRipple(),
                onClick = onClick,
            )
            .padding(
                horizontal = dimensionResource(R.dimen.little_margin),
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.secondary,
                unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                disabledColor = MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.disabled)
            )
        )
        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.little_margin)))
        Text(
            text = text,
            color = DayNightTheme.colors.titleColor,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

private fun timeToString(
    hour: Int,
    minute: Int
) = when {
    hour == 0 -> "00"
    hour < 10 -> "0$hour"
    else -> hour.toString()
} + ":" + when {
    minute == 0 -> "00"
    minute < 10 -> "0$minute"
    else -> minute.toString()
}

private fun stringToTime(
    time: String
) = time.split(":").map { it.toIntOrNull() ?: 0 }

@Composable
fun TimePickerPreferenceView(
    title: String,
    summary: (String) -> String?, // currentTime (xx:xx) -> summary.
    currentTime: String,
    enabled: Boolean = true,
    onValueChanged: (String) -> Unit,
) {

    val currentTimeState = remember { mutableStateOf(currentTime) }
    val dialogOpenState = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberThemeRipple(),
                onClick = { dialogOpenState.value = true },
                enabled = enabled,
            )
            .padding(dimensionResource(R.dimen.normal_margin)),
        verticalArrangement = Arrangement.Center,
    ) {
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = DayNightTheme.colors.titleColor,
                style = MaterialTheme.typography.titleMedium,
            )
            val currentSummary = summary(currentTimeState.value)
            if (currentSummary?.isNotEmpty() == true) {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.little_margin)))
                Text(
                    text = currentSummary,
                    color = DayNightTheme.colors.captionColor,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }

    if (dialogOpenState.value) {
        val pickerTimeState = remember { mutableStateOf(currentTimeState.value) }

        AlertDialog(
            onDismissRequest = { dialogOpenState.value = false },
            title = {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineSmall,
                )
            },
            text = {
                AndroidView(
                    factory = { context ->
                        val timePicker = TimePicker(
                            context,
                            null,
                            R.style.Widget_Material3_MaterialTimePicker
                        )
                        timePicker.setIs24HourView(true)
                        timePicker.setOnTimeChangedListener { _, hour, minute ->
                            pickerTimeState.value = timeToString(hour = hour, minute = minute)
                        }

                        val time = stringToTime(currentTimeState.value)
                        timePicker.currentHour = time.elementAtOrNull(0) ?: 0
                        timePicker.currentMinute = time.elementAtOrNull(1) ?: 0
                        pickerTimeState.value = timeToString(
                            hour = timePicker.currentHour,
                            minute = timePicker.currentMinute
                        )

                        timePicker
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        currentTimeState.value = pickerTimeState.value
                        dialogOpenState.value = false
                        onValueChanged(currentTimeState.value)
                    }
                ) {
                    Text(
                        text = stringResource(R.string.done),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { dialogOpenState.value = false }
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        )
    }
}

@Composable
fun EditTextPreferenceView(
    title: String,
    summary: (String) -> String?, // content -> summary.
    content: String,
    enabled: Boolean = true,
    onValueChanged: (String) -> Unit,
) {
    val contentState = remember { mutableStateOf(content) }
    val dialogOpenState = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberThemeRipple(),
                onClick = { dialogOpenState.value = true },
                enabled = enabled,
            )
            .padding(dimensionResource(R.dimen.normal_margin)),
        verticalArrangement = Arrangement.Center,
    ) {
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = DayNightTheme.colors.titleColor,
                style = MaterialTheme.typography.titleMedium,
            )
            val currentSummary = summary(contentState.value)
            if (currentSummary?.isNotEmpty() == true) {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.little_margin)))
                Text(
                    text = currentSummary,
                    color = DayNightTheme.colors.captionColor,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }

    if (dialogOpenState.value) {
        val inputState = remember { mutableStateOf(contentState.value) }
        AlertDialog(
            onDismissRequest = { dialogOpenState.value = false },
            title = {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineSmall,
                )
            },
            text = {
                OutlinedTextField(
                    value = inputState.value,
                    onValueChange = { inputState.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.little_margin)),
                    readOnly = false,
                    enabled = true,
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        errorCursorColor = MaterialTheme.colorScheme.error,
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(
                            alpha = ContentAlpha.high
                        ),
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(
                            alpha = ContentAlpha.disabled
                        ),
                        disabledBorderColor = MaterialTheme.colorScheme.onSurface.copy(
                            alpha = ContentAlpha.disabled
                        ),
                        errorBorderColor = MaterialTheme.colorScheme.error,
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        contentState.value = inputState.value
                        dialogOpenState.value = false
                        onValueChanged(contentState.value)
                    }
                ) {
                    Text(
                        text = stringResource(R.string.done),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { dialogOpenState.value = false }
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        )
    }
}

@Preview
@Composable
private fun DefaultPreview() {
    GeometricWeatherTheme(lightTheme = isSystemInDarkTheme()) {
        Column(
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        ) {
            CheckboxPreferenceView(
                title = "Checkbox",
                summary = { "currentSeldhauidhiashdiuashduiashdiuahsidhsaiudhasuihdiuashdiusahduiashdiusahduihasudhasudhsahduashdiuashdiuhaected = $it" },
                checked = false,
                onValueChanged = { /* do nothing */ }
            )
            ListPreferenceView(
                title = "List Selector",
                summary = { "currentSelected = $it" },
                selectedKey = "a",
                keyNamePairList = listOf("a", "b", "c").zip(listOf("A", "B", "C")),
                onValueChanged = { /* do nothing */ }
            )
            TimePickerPreferenceView(
                title = "TimePicker",
                summary = { "currentTime = $it" },
                currentTime = "08:00",
                onValueChanged = { /* do nothing */ }
            )
        }
    }
}