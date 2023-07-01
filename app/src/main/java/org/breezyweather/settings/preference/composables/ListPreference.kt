package org.breezyweather.settings.preference.composables

import android.content.Context
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.breezyweather.R
import org.breezyweather.common.ui.widgets.Material3CardListItem
import org.breezyweather.common.ui.widgets.defaultCardListItemElevation
import org.breezyweather.theme.compose.DayNightTheme
import org.breezyweather.theme.compose.rememberThemeRipple

@Composable
fun ListPreferenceView(
    @StringRes titleId: Int,
    @ArrayRes valueArrayId: Int,
    @ArrayRes nameArrayId: Int,
    @ArrayRes summaryArrayId: Int? = null,
    selectedKey: String,
    enabled: Boolean = true,
    onValueChanged: (String) -> Unit,
) {
    val values = stringArrayResource(valueArrayId)
    val names = stringArrayResource(nameArrayId)
    val summaries = if (summaryArrayId == null) names else stringArrayResource(summaryArrayId)
    ListPreferenceView(
        title = stringResource(titleId),
        summary = { _, value -> summaries[values.indexOfFirst { it == value }] },
        selectedKey = selectedKey,
        valueArray = values,
        nameArray = names,
        enabled = enabled,
        onValueChanged = onValueChanged,
    )
}

@Composable
fun ListPreferenceView(
    title: String,
    summary: (Context, String) -> String?, // value -> summary.
    selectedKey: String,
    valueArray: Array<String>,
    nameArray: Array<String>,
    enabled: Boolean = true,
    onValueChanged: (String) -> Unit,
) {
    val listSelectedState = remember { mutableStateOf(selectedKey) }
    val dialogOpenState = remember { mutableStateOf(false) }

    Material3CardListItem(
        elevation = if (enabled) defaultCardListItemElevation else 0.dp
    ) {
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
                    color = DayNightTheme.colors.titleColor,
                    style = MaterialTheme.typography.titleMedium,
                )
                val currentSummary = summary(LocalContext.current, listSelectedState.value)
                if (currentSummary?.isNotEmpty() == true) {
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.little_margin)))
                    Text(
                        text = currentSummary,
                        color = DayNightTheme.colors.bodyColor,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
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
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(valueArray.zip(nameArray)) {
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
                        text = stringResource(R.string.action_cancel),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        )
    }
}

@Composable
internal fun RadioButton(
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
        androidx.compose.material3.RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.little_margin)))
        Text(
            text = text,
            color = DayNightTheme.colors.titleColor,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}