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

package org.breezyweather.settings.preference.composables

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.breezyweather.R
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.toBitmap
import org.breezyweather.common.ui.composables.AlertDialogNoPadding
import org.breezyweather.common.ui.widgets.Material3CardListItem
import org.breezyweather.common.ui.widgets.defaultCardListItemElevation
import org.breezyweather.theme.compose.DayNightTheme
import org.breezyweather.theme.compose.themeRipple
import java.text.Collator

data class PreferenceItem(
    val name: String,
    val value: String,
    val icon: Drawable? = null,
    val subname: String? = null,
)

@Composable
fun MultiListPreferenceView(
    title: String,
    @DrawableRes iconId: Int? = null,
    selectedKeys: List<String>,
    itemsArray: Array<PreferenceItem>,
    noItemsMessage: String,
    enabled: Boolean = true,
    card: Boolean = true,
    onValueChanged: (List<String>) -> Unit,
) {
    val context = LocalContext.current
    val listSelectedState = remember {
        mutableStateListOf<String>().apply {
            addAll(selectedKeys)
        }
    }
    val dialogOpenState = remember { mutableStateOf(false) }
    val currentSummary = listSelectedState.mapNotNull { selectedItem ->
        itemsArray.firstOrNull { it.value == selectedItem }?.name
    }.sortedWith(Collator.getInstance(context.currentLocale))
        .joinToString(stringResource(R.string.comma_separator))
        .ifEmpty {
            stringResource(R.string.settings_disabled)
        }

    // TODO: Redundancy
    if (card) {
        Material3CardListItem(
            elevation = if (enabled) defaultCardListItemElevation else 0.dp
        ) {
            ListItem(
                tonalElevation = if (enabled) defaultCardListItemElevation else 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (enabled) 1f else 0.5f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = themeRipple(),
                        onClick = { dialogOpenState.value = true },
                        enabled = enabled
                    )
                    .padding(PaddingValues(vertical = 8.dp)),
                leadingContent = if (iconId != null) {
                    {
                        Icon(
                            painter = painterResource(iconId),
                            tint = DayNightTheme.colors.titleColor,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    null
                },
                headlineContent = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = title,
                                color = DayNightTheme.colors.titleColor,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                },
                supportingContent = {
                    Column {
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.little_margin)))
                        Text(
                            text = currentSummary,
                            color = DayNightTheme.colors.bodyColor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            )
        }
    } else {
        ListItem(
            tonalElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (enabled) 1f else 0.5f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = themeRipple(),
                    onClick = { dialogOpenState.value = true },
                    enabled = enabled
                )
                .padding(PaddingValues(vertical = 8.dp)),
            leadingContent = if (iconId != null) {
                {
                    Icon(
                        painter = painterResource(iconId),
                        tint = DayNightTheme.colors.titleColor,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                null
            },
            headlineContent = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = title,
                            color = DayNightTheme.colors.titleColor,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            },
            supportingContent = {
                Column {
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.little_margin)))
                    Text(
                        text = currentSummary,
                        color = DayNightTheme.colors.bodyColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        )
    }

    if (dialogOpenState.value) {
        if (itemsArray.isNotEmpty()) {
            AlertDialogNoPadding(
                onDismissRequest = {
                    listSelectedState.apply {
                        clear()
                        addAll(selectedKeys)
                    }
                    dialogOpenState.value = false
                },
                title = {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                text = {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(itemsArray) {
                            Switch(
                                icon = it.icon,
                                selected = listSelectedState.contains(it.value),
                                onClick = {
                                    if (listSelectedState.contains(it.value)) {
                                        listSelectedState.remove(it.value)
                                    } else {
                                        listSelectedState.add(it.value)
                                    }
                                },
                                text = it.name,
                                subtext = it.subname
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onValueChanged(listSelectedState)
                            dialogOpenState.value = false
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.action_save),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            listSelectedState.apply {
                                clear()
                                addAll(selectedKeys)
                            }
                            dialogOpenState.value = false
                        }
                    ) {
                        Text(
                            text = stringResource(android.R.string.cancel),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = {
                    dialogOpenState.value = false
                },
                title = {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                text = {
                    Text(
                        text = noItemsMessage,
                        color = DayNightTheme.colors.bodyColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            dialogOpenState.value = false
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.action_close),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            )
        }
    }
}

@Composable
internal fun Switch(
    selected: Boolean,
    onClick: () -> Unit,
    text: String,
    icon: Drawable? = null,
    subtext: String? = null,
) {
    ListItem(
        headlineContent = {
            Text(text)
        },
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = themeRipple(),
                onClick = onClick
            ),
        leadingContent = {
            icon?.toBitmap()?.asImageBitmap()?.let { bitmap ->
                Image(
                    bitmap,
                    contentDescription = text,
                    modifier = Modifier
                        .height(42.dp)
                        .width(42.dp)
                )
            }
        },
        trailingContent = {
            androidx.compose.material3.Switch(
                checked = selected,
                onCheckedChange = {
                    onClick()
                }
            )
        }
    )
}

@Composable
fun PackagePreferenceView(
    title: String,
    selectedKeys: List<String>,
    intent: String,
    onValueChanged: (List<String>) -> Unit,
) {
    val context = LocalContext.current
    val packages = remember {
        context.packageManager.queryBroadcastReceivers(Intent(intent), PackageManager.GET_RESOLVED_FILTER)
    }

    MultiListPreferenceView(
        title = title,
        selectedKeys = selectedKeys,
        itemsArray = packages
            .sortedWith { app1, app2 ->
                Collator.getInstance(context.currentLocale).compare(
                    app1.activityInfo.applicationInfo.loadLabel(context.packageManager),
                    app2.activityInfo.applicationInfo.loadLabel(context.packageManager)
                )
            }
            .map {
                PreferenceItem(
                    name = it.activityInfo.applicationInfo.loadLabel(context.packageManager).toString(),
                    subname = it.activityInfo.applicationInfo.packageName,
                    value = it.activityInfo.applicationInfo.packageName,
                    icon = it.activityInfo.applicationInfo.loadIcon(context.packageManager)
                )
            }.toTypedArray(),
        noItemsMessage = stringResource(R.string.settings_widgets_broadcast_send_data_summary_empty),
        onValueChanged = onValueChanged
    )
}
