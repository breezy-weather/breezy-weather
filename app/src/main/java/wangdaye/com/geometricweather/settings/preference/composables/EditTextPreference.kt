package wangdaye.com.geometricweather.settings.preference.composables

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.ContentAlpha
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.theme.compose.DayNightTheme
import wangdaye.com.geometricweather.theme.compose.rememberThemeRipple

@Composable
fun EditTextPreferenceView(
    @StringRes titleId: Int,
    summary: ((Context, String) -> String?)? = null,
    content: String,
    enabled: Boolean = true,
    onValueChanged: (String) -> Unit,
) = EditTextPreferenceView(
    title = stringResource(titleId),
    summary = { context, value ->
        summary?.let { it(context, value) }
    },
    content = content,
    enabled = enabled,
    onValueChanged = onValueChanged,
)

@Composable
fun EditTextPreferenceView(
    title: String,
    summary: (Context, String) -> String?, // content -> summary.
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
            val currentSummary = summary(LocalContext.current, contentState.value)
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