package org.breezyweather.settings.compose

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import org.breezyweather.BreezyWeather
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.DarkMode
import org.breezyweather.common.basic.models.options.appearance.*
import org.breezyweather.common.utils.helpers.AsyncHelper
import org.breezyweather.common.utils.helpers.SnackbarHelper
import org.breezyweather.settings.SettingsManager
import org.breezyweather.settings.dialogs.ProvidersPreviewerDialog
import org.breezyweather.settings.preference.bottomInsetItem
import org.breezyweather.settings.preference.clickablePreferenceItem
import org.breezyweather.settings.preference.composables.ListPreferenceView
import org.breezyweather.settings.preference.composables.PreferenceView
import org.breezyweather.settings.preference.listPreferenceItem
import org.breezyweather.theme.ThemeManager
import org.breezyweather.theme.resource.ResourcesProviderFactory

@Composable
fun AppearanceSettingsScreen(
    context: Context,
    navController: NavHostController,
    paddingValues: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentPadding = paddingValues,
    ) {
        listPreferenceItem(R.string.settings_appearance_language_title) { id ->
            ListPreferenceView(
                titleId = id,
                valueArrayId = R.array.language_values,
                nameArrayId = R.array.languages,
                selectedKey = SettingsManager.getInstance(context).language.id,
                onValueChanged = {
                    SettingsManager.getInstance(context).language = Language.getInstance(it)

                    SnackbarHelper.showSnackbar(
                        content = context.getString(R.string.settings_changes_apply_after_restart),
                        action = context.getString(R.string.action_restart)
                    ) {
                        BreezyWeather.instance.recreateAllActivities()
                    }
                },
            )
        }
        listPreferenceItem(R.string.settings_appearance_dark_mode_title) { id ->
            ListPreferenceView(
                titleId = id,
                selectedKey = SettingsManager.getInstance(context).darkMode.id,
                valueArrayId = R.array.dark_mode_values,
                nameArrayId = R.array.dark_modes,
                onValueChanged = {
                    SettingsManager
                        .getInstance(context)
                        .darkMode = DarkMode.getInstance(it)

                    AsyncHelper.delayRunOnUI({
                        ThemeManager
                            .getInstance(context)
                            .update(darkMode = SettingsManager.getInstance(context).darkMode)
                    },300)
                },
            )
        }
        clickablePreferenceItem(
            R.string.settings_appearance_icon_pack_title
        ) {
            val iconProviderState = remember {
                mutableStateOf(
                    SettingsManager.getInstance(context).iconProvider
                )
            }

            PreferenceView(
                title = stringResource(it),
                summary = ResourcesProviderFactory
                    .getNewInstance(iconProviderState.value)
                    .providerName
            ) {
                (context as? Activity)?.let { activity ->
                    ProvidersPreviewerDialog.show(activity) { packageName ->
                        SettingsManager.getInstance(context).iconProvider = packageName
                        iconProviderState.value = packageName
                    }
                }
            }
        }
        clickablePreferenceItem(R.string.settings_units) { id ->
            PreferenceView(
                titleId = id,
                summaryId = R.string.settings_units_summary
            ) {
                navController.navigate(SettingsScreenRouter.Unit.route)
            }
        }

        bottomInsetItem()
    }
}