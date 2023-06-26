package org.breezyweather.settings.compose

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.breezyweather.R
import org.breezyweather.background.polling.PollingManager
import org.breezyweather.common.basic.models.options.BackgroundUpdateMethod
import org.breezyweather.common.basic.models.options.UpdateInterval
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.settings.SettingsManager
import org.breezyweather.settings.preference.bottomInsetItem
import org.breezyweather.settings.preference.composables.ListPreferenceView
import org.breezyweather.settings.preference.composables.PreferenceScreen
import org.breezyweather.settings.preference.listPreferenceItem
import org.breezyweather.settings.preference.switchPreferenceItem

@Composable
fun BackgroundSettingsScreen(
    context: Context,
    paddingValues: PaddingValues,
    postNotificationPermissionEnsurer: (succeedCallback: () -> Unit) -> Unit,
) = PreferenceScreen(paddingValues = paddingValues) {
    switchPreferenceItem(R.string.settings_background_updates_methods_title) { id ->
        ListPreferenceView(
            titleId = id,
            selectedKey = SettingsManager.getInstance(context).backgroundUpdateMethod.id,
            valueArrayId = R.array.background_update_method_values,
            nameArrayId = R.array.background_update_methods,
            onValueChanged = {
                val newBackgroundUpdateMethod = BackgroundUpdateMethod.getInstance(it)
                SettingsManager.getInstance(context).backgroundUpdateMethod = newBackgroundUpdateMethod
                if (newBackgroundUpdateMethod == BackgroundUpdateMethod.NOTIFICATION) {
                    postNotificationPermissionEnsurer {
                        PollingManager.resetNormalBackgroundTask(context, false)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            showBlockNotificationGroupDialog(context)
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            showIgnoreBatteryOptimizationDialog(context)
                        }
                    }
                }
            },
        )
    }
    listPreferenceItem(R.string.settings_background_updates_refresh_title) { id ->
        ListPreferenceView(
            titleId = id,
            selectedKey = SettingsManager.getInstance(context).updateInterval.id,
            valueArrayId = R.array.automatic_refresh_rate_values,
            nameArrayId = R.array.automatic_refresh_rates,
            onValueChanged = {
                SettingsManager
                    .getInstance(context)
                    .updateInterval = UpdateInterval.getInstance(it)
            },
        )
    }

    bottomInsetItem()
}

@RequiresApi(api = Build.VERSION_CODES.O)
private fun showBlockNotificationGroupDialog(context: Context) {
    MaterialAlertDialogBuilder(context)
        .setTitle(R.string.dialog_permissions_block_notification_channel_title)
        .setMessage(R.string.dialog_permissions_block_notification_channel_content)
        .setPositiveButton(R.string.action_set) { _, _ ->
            val intent = Intent()
            intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            intent.putExtra(
                Settings.EXTRA_APP_PACKAGE,
                context.packageName
            )
            context.startActivity(intent)
            showIgnoreBatteryOptimizationDialog(context)
        }
        .setNeutralButton(
            R.string.action_done
        ) { _, _ ->
            showIgnoreBatteryOptimizationDialog(context)
        }
        .setCancelable(false)
        .show()
}

@RequiresApi(api = Build.VERSION_CODES.M)
private fun showIgnoreBatteryOptimizationDialog(context: Context) {
    MaterialAlertDialogBuilder(context)
        .setTitle(R.string.dialog_permissions_ignore_battery_optimizations_title)
        .setMessage(R.string.dialog_permissions_ignore_battery_optimizations_content)
        .setPositiveButton(
            R.string.action_set
        ) { _, _ ->
            IntentHelper.startBatteryOptimizationActivity(context)
        }
        .setNeutralButton(R.string.action_done) { _, _ -> }
        .setCancelable(false)
        .show()
}
