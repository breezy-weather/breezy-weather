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

package org.breezyweather.ui.settings.compose

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.breezyweather.BreezyWeather
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.DarkMode
import org.breezyweather.common.extensions.plus
import org.breezyweather.common.extensions.toBitmap
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.common.composables.AlertDialogLink
import org.breezyweather.ui.common.composables.AlertDialogNoPadding
import org.breezyweather.ui.common.widgets.Material3Scaffold
import org.breezyweather.ui.common.widgets.generateCollapsedScrollBehavior
import org.breezyweather.ui.common.widgets.insets.FitStatusBarTopAppBar
import org.breezyweather.ui.settings.preference.bottomInsetItem
import org.breezyweather.ui.settings.preference.clickablePreferenceItem
import org.breezyweather.ui.settings.preference.composables.CalendarPreferenceView
import org.breezyweather.ui.settings.preference.composables.LanguagePreferenceView
import org.breezyweather.ui.settings.preference.composables.ListPreferenceView
import org.breezyweather.ui.settings.preference.composables.PreferenceScreen
import org.breezyweather.ui.settings.preference.composables.PreferenceViewWithCard
import org.breezyweather.ui.settings.preference.composables.SwitchPreferenceView
import org.breezyweather.ui.settings.preference.largeSeparatorItem
import org.breezyweather.ui.settings.preference.listPreferenceItem
import org.breezyweather.ui.settings.preference.sectionFooterItem
import org.breezyweather.ui.settings.preference.sectionHeaderItem
import org.breezyweather.ui.settings.preference.smallSeparatorItem
import org.breezyweather.ui.settings.preference.switchPreferenceItem
import org.breezyweather.ui.theme.resource.ResourcesProviderFactory
import org.breezyweather.ui.theme.resource.providers.ResourceProvider

@Composable
fun AppearanceSettingsScreen(
    context: Context,
    onNavigateTo: (route: String) -> Unit,
    onNavigateBack: () -> Unit,
    darkMode: DarkMode,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = generateCollapsedScrollBehavior()

    Material3Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            FitStatusBarTopAppBar(
                title = stringResource(R.string.settings_appearance),
                onBackPressed = onNavigateBack,
                actions = { AboutActivityIconButton(context) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddings ->
        PreferenceScreen(
            paddingValues = paddings.plus(PaddingValues(horizontal = dimensionResource(R.dimen.normal_margin)))
        ) {
            sectionHeaderItem(R.string.settings_appearance_section_regional)
            listPreferenceItem(R.string.settings_appearance_language_title) { id ->
                LanguagePreferenceView(
                    titleId = id,
                    isFirst = true
                )
            }
            smallSeparatorItem()
            clickablePreferenceItem(R.string.settings_units) { id ->
                PreferenceViewWithCard(
                    titleId = id,
                    summaryId = R.string.settings_units_summary
                ) {
                    onNavigateTo(SettingsScreenRouter.Unit.route)
                }
            }
            smallSeparatorItem()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                listPreferenceItem(R.string.settings_appearance_calendar_title) { id ->
                    CalendarPreferenceView(
                        titleId = id,
                        isLast = true
                    )
                }
            }
            sectionFooterItem(R.string.settings_appearance_section_regional)

            largeSeparatorItem()

            sectionHeaderItem(R.string.settings_appearance_section_theme)
            listPreferenceItem(R.string.settings_appearance_dark_mode_title) { id ->
                ListPreferenceView(
                    titleId = id,
                    selectedKey = darkMode.id,
                    valueArrayId = R.array.dark_mode_values,
                    nameArrayId = R.array.dark_modes,
                    card = true,
                    isFirst = true,
                    withState = false,
                    onValueChanged = {
                        val newDarkMode = DarkMode.getInstance(it)
                        SettingsManager.getInstance(context).darkMode = newDarkMode
                        BreezyWeather.instance.updateDayNightMode(newDarkMode.value)
                    }
                )
            }
            smallSeparatorItem()
            switchPreferenceItem(R.string.settings_appearance_dark_mode_locations_title) { id ->
                SwitchPreferenceView(
                    titleId = id,
                    summaryOnId = if (darkMode == DarkMode.LIGHT) {
                        R.string.settings_appearance_dark_mode_locations_always_enabled
                    } else {
                        R.string.settings_enabled
                    },
                    summaryOffId = R.string.settings_appearance_dark_mode_locations_disabled,
                    // TODO: Always true when selected dark mode is “Always light”:
                    checked = SettingsManager.getInstance(context).dayNightModeForLocations ||
                        darkMode == DarkMode.LIGHT,
                    enabled = darkMode != DarkMode.LIGHT,
                    onValueChanged = {
                        SettingsManager.getInstance(context).dayNightModeForLocations = it
                    }
                )
            }
            smallSeparatorItem()
            clickablePreferenceItem(
                R.string.settings_appearance_icon_pack_title
            ) {
                val dialogIconPackOpenState = rememberSaveable { mutableStateOf(false) }
                val dialogLinkOpenState = rememberSaveable { mutableStateOf(false) }
                val iconProviderState = rememberSaveable {
                    mutableStateOf(SettingsManager.getInstance(context).iconProvider)
                }
                val listProviderState = remember {
                    mutableStateOf(listOf<ResourceProvider>())
                }

                PreferenceViewWithCard(
                    title = stringResource(it),
                    summary = ResourcesProviderFactory
                        .getNewInstance(iconProviderState.value)
                        .providerName,
                    isLast = true
                ) {
                    dialogIconPackOpenState.value = true
                    /*(context as? Activity)?.let { activity ->
                        ProvidersPreviewerDialog.show(activity) { packageName ->
                            SettingsManager.getInstance(context).iconProvider = packageName
                            iconProviderState.value = packageName
                        }
                    }*/
                }
                if (dialogIconPackOpenState.value) {
                    // TODO: async
                    listProviderState.value = ResourcesProviderFactory.getProviderList(BreezyWeather.instance)

                    AlertDialogNoPadding(
                        onDismissRequest = {
                            dialogIconPackOpenState.value = false
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    dialogLinkOpenState.value = true
                                }
                            ) {
                                Text(
                                    text = stringResource(R.string.settings_icon_packs_get_more),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    dialogIconPackOpenState.value = false
                                }
                            ) {
                                Text(
                                    text = stringResource(R.string.action_close),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        },
                        title = {
                            Text(
                                text = stringResource(R.string.settings_icon_packs_title),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        },
                        text = {
                            // 3 columns: icon / name / search icon
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(), // .fillMaxHeight()
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(listProviderState.value) {
                                    ListItem(
                                        colors = ListItemDefaults.colors(AlertDialogDefaults.containerColor),
                                        headlineContent = {
                                            Text(it.providerName ?: "")
                                        },
                                        modifier = Modifier.clickable {
                                            SettingsManager.getInstance(context).iconProvider = it.packageName
                                            iconProviderState.value = it.packageName
                                            dialogIconPackOpenState.value = false
                                        },
                                        leadingContent = {
                                            it.providerIcon?.toBitmap()?.asImageBitmap()?.let { bitmap ->
                                                Image(
                                                    bitmap,
                                                    contentDescription = it.providerName,
                                                    modifier = Modifier
                                                        .height(42.dp)
                                                        .width(42.dp)
                                                )
                                            }
                                        },
                                        trailingContent = {
                                            IconButton(
                                                onClick = {
                                                    IntentHelper.startPreviewIconActivity(
                                                        context as Activity,
                                                        it.packageName
                                                    )
                                                },
                                                modifier = Modifier.clip(CircleShape)
                                            ) {
                                                Icon(
                                                    painterResource(R.drawable.ic_search),
                                                    contentDescription = stringResource(
                                                        R.string.settings_icon_packs_check_details
                                                    ),
                                                    tint = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    )
                }
                if (dialogLinkOpenState.value) {
                    AlertDialogLink(
                        onClose = { dialogLinkOpenState.value = false },
                        linkToOpen = "https://github.com/breezy-weather/breezy-weather-icon-packs/blob/main/README.md"
                    )
                }
            }
            sectionFooterItem(R.string.settings_appearance_section_theme)

            bottomInsetItem()
        }
    }
}
