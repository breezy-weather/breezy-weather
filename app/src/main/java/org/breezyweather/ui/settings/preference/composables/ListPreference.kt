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

import android.content.Context
import android.os.Build
import androidx.annotation.ArrayRes
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.appearance.CalendarHelper
import org.breezyweather.common.basic.models.options.appearance.LocaleHelper
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.common.composables.AlertDialogNoPadding
import org.breezyweather.ui.common.widgets.Material3ExpressiveCardListItem
import org.breezyweather.ui.common.widgets.defaultCardListItemElevation
import org.breezyweather.ui.theme.compose.themeRipple
import java.util.Date

@Composable
fun ListPreferenceView(
    @StringRes titleId: Int,
    @ArrayRes valueArrayId: Int,
    @ArrayRes nameArrayId: Int,
    selectedKey: String,
    @ArrayRes summaryArrayId: Int? = null,
    @DrawableRes iconId: Int? = null,
    enabled: Boolean = true,
    card: Boolean = false,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    colors: ListItemColors = ListItemDefaults.colors(),
    withState: Boolean = true,
    onValueChanged: (String) -> Unit,
) {
    val values = stringArrayResource(valueArrayId)
    val names = stringArrayResource(nameArrayId)
    val summaries = if (summaryArrayId == null) names else stringArrayResource(summaryArrayId)
    if (card) {
        ListPreferenceViewWithCard(
            title = stringResource(titleId),
            iconId = iconId,
            summary = { _, value -> summaries[values.indexOfFirst { it == value }] },
            selectedKey = selectedKey,
            valueArray = values,
            nameArray = names,
            enabled = enabled,
            isFirst = isFirst,
            isLast = isLast,
            colors = colors,
            withState = withState,
            onValueChanged = onValueChanged
        )
    } else {
        ListPreferenceView(
            title = stringResource(titleId),
            iconId = iconId,
            summary = { _, value -> summaries[values.indexOfFirst { it == value }] },
            selectedKey = selectedKey,
            valueArray = values,
            nameArray = names,
            enabled = enabled,
            colors = colors,
            withState = withState,
            onValueChanged = onValueChanged
        )
    }
}

@Composable
fun ListPreferenceViewWithCard(
    title: String,
    summary: (Context, String) -> String?, // value -> summary.
    selectedKey: String,
    valueArray: Array<String>,
    nameArray: Array<String>,
    @DrawableRes iconId: Int? = null,
    enableArray: Array<Boolean>? = null,
    enabled: Boolean = true,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    colors: ListItemColors = ListItemDefaults.colors(),
    withState: Boolean = true,
    dismissButton: @Composable (() -> Unit)? = null,
    onValueChanged: (String) -> Unit,
) {
    Material3ExpressiveCardListItem(
        elevation = if (enabled) defaultCardListItemElevation else 0.dp,
        isFirst = isFirst,
        isLast = isLast
    ) {
        ListPreferenceView(
            title = title,
            summary = summary,
            selectedKey = selectedKey,
            valueArray = valueArray,
            nameArray = nameArray,
            iconId = iconId,
            enableArray = enableArray,
            enabled = enabled,
            card = true,
            colors = colors,
            withState = withState,
            dismissButton = dismissButton,
            onValueChanged = onValueChanged
        )
    }
}

@Composable
fun ListPreferenceView(
    title: String,
    summary: (Context, String) -> String?, // value -> summary.
    selectedKey: String,
    valueArray: Array<String>,
    nameArray: Array<String>,
    modifier: Modifier = Modifier,
    @DrawableRes iconId: Int? = null,
    enableArray: Array<Boolean>? = null,
    enabled: Boolean = true,
    card: Boolean = false,
    colors: ListItemColors = ListItemDefaults.colors(),
    withState: Boolean = true,
    dismissButton: @Composable (() -> Unit)? = null,
    onValueChanged: (String) -> Unit,
) {
    val listSelectedState = remember { mutableStateOf(selectedKey) }
    val dialogOpenState = remember { mutableStateOf(false) }
    val currentSummary = summary(LocalContext.current, if (withState) listSelectedState.value else selectedKey)

    ListItem(
        colors = colors,
        tonalElevation = if (card && enabled) defaultCardListItemElevation else 0.dp,
        modifier = modifier
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
                    tint = MaterialTheme.colorScheme.onSurface,
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
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        },
        supportingContent = if (currentSummary?.isNotEmpty() == true) {
            {
                Column {
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.small_margin)))
                    Text(
                        text = currentSummary,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            null
        }
    )

    if (dialogOpenState.value) {
        AlertDialogNoPadding(
            onDismissRequest = { dialogOpenState.value = false },
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
                    itemsIndexed(valueArray.zip(nameArray)) { i, it ->
                        // Special case with groups
                        if (it.second.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = dimensionResource(R.dimen.small_margin)
                                    )
                            ) {
                                if (i != 0) {
                                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.large_margin)))
                                } else {
                                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.small_margin)))
                                }
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = it.first,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.titleSmall,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            RadioButton(
                                enabled = enableArray?.getOrNull(i) ?: true,
                                selected = if (withState) {
                                    listSelectedState.value == it.first
                                } else {
                                    selectedKey == it.first
                                },
                                onClick = {
                                    if (enableArray?.getOrNull(i) != false) {
                                        if (withState) {
                                            listSelectedState.value = it.first
                                        }
                                        dialogOpenState.value = false
                                        onValueChanged(it.first)
                                    }
                                },
                                text = it.second
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { dialogOpenState.value = false }
                ) {
                    Text(
                        text = stringResource(android.R.string.cancel),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            dismissButton = dismissButton
        )
    }
}

@Composable
fun ListPreferenceWithGroupsView(
    title: String,
    summary: (Context, String) -> String?, // value -> summary.
    selectedKey: String,
    values: ImmutableMap<String?, ImmutableList<Triple<String, String, Boolean>>>,
    modifier: Modifier = Modifier,
    @DrawableRes iconId: Int? = null,
    enabled: Boolean = true,
    colors: ListItemColors = ListItemDefaults.colors(),
    withState: Boolean = true,
    dismissButton: @Composable (() -> Unit)? = null,
    onValueChanged: (String) -> Unit,
) {
    val keyList = mutableListOf<String>()
    val nameList = mutableListOf<String>()
    val enableList = mutableListOf<Boolean>()
    for ((k1, v1) in values) {
        if (k1 != null) {
            // Typed array, so no null; we use an empty value
            // Since it's possible to have empty keys, we must do it the other way around for the
            // ListPreferenceView to recognize our group
            keyList.add(k1)
            nameList.add("")
            enableList.add(false)
        }
        for (triple in v1) {
            keyList.add(triple.first)
            nameList.add(triple.second)
            enableList.add(triple.third)
        }
    }

    return ListPreferenceView(
        modifier = modifier,
        title = title,
        iconId = iconId,
        summary = summary,
        selectedKey = selectedKey,
        valueArray = keyList.toTypedArray(),
        nameArray = nameList.toTypedArray(),
        enableArray = enableList.toTypedArray(),
        enabled = enabled,
        colors = colors,
        withState = withState,
        dismissButton = dismissButton,
        onValueChanged = onValueChanged
    )
}

@Composable
internal fun RadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    text: String,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = themeRipple(),
                onClick = onClick
            )
            .padding(
                horizontal = dimensionResource(R.dimen.small_margin)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.RadioButton(
            enabled = enabled,
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.small_margin)))
        Text(
            text = text,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            },
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun LanguagePreferenceView(
    @StringRes titleId: Int,
    isFirst: Boolean = false,
    isLast: Boolean = false,
) {
    val context = LocalContext.current

    val langs = remember { LocaleHelper.getLangs(context) }
    val currentLanguage by remember {
        mutableStateOf(AppCompatDelegate.getApplicationLocales().get(0)?.toLanguageTag() ?: "")
    }

    ListPreferenceViewWithCard(
        title = stringResource(titleId),
        summary = { _, value -> langs.firstOrNull { value == it.langTag }?.displayName ?: "" },
        selectedKey = langs.firstOrNull { currentLanguage == it.langTag }?.langTag ?: "",
        valueArray = langs.map { it.langTag }.toTypedArray(),
        nameArray = langs.map { it.displayName }.toTypedArray(),
        isFirst = isFirst,
        isLast = isLast
    ) {
        val locale = if (it.isEmpty()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(it)
        }
        AppCompatDelegate.setApplicationLocales(locale)
        SettingsManager.getInstance(context).languageUpdateLastTimestamp = Date().time
    }
}

@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun CalendarPreferenceView(
    @StringRes titleId: Int,
    isFirst: Boolean = false,
    isLast: Boolean = false,
) {
    val context = LocalContext.current

    val calendars = remember { CalendarHelper.getCalendars(context) }
    val currentCalendar by remember {
        mutableStateOf(SettingsManager.getInstance(context).alternateCalendar)
    }

    ListPreferenceViewWithCard(
        title = stringResource(titleId),
        summary = { _, value -> calendars.firstOrNull { value == it.id }?.displayName ?: "" },
        selectedKey = calendars.firstOrNull { currentCalendar == it.id }?.id ?: "",
        valueArray = calendars.map { it.id }.toTypedArray(),
        nameArray = calendars.map { it.displayName }.toTypedArray(),
        isFirst = isFirst,
        isLast = isLast
    ) {
        SettingsManager.getInstance(context).alternateCalendar = it
    }
}
