package org.breezyweather.settings.compose

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.core.net.toUri
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.breezyweather.R
import org.breezyweather.background.polling.PollingManager
import org.breezyweather.common.basic.models.options.BackgroundUpdateMethod
import org.breezyweather.common.basic.models.options.UpdateInterval
import org.breezyweather.common.utils.helpers.SnackbarHelper
import org.breezyweather.settings.SettingsManager
import org.breezyweather.settings.activities.WorkerInfoActivity
import org.breezyweather.settings.preference.*
import org.breezyweather.settings.preference.composables.ListPreferenceView
import org.breezyweather.settings.preference.composables.PreferenceScreen
import org.breezyweather.settings.preference.composables.PreferenceView
import org.breezyweather.extensions.powerManager

@Composable
fun BackgroundSettingsScreen(
    context: Context,
    paddingValues: PaddingValues,
    postNotificationPermissionEnsurer: (succeedCallback: () -> Unit) -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    PreferenceScreen(paddingValues = paddingValues) {
        sectionHeaderItem(R.string.settings_background_updates_section_general)
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
        sectionFooterItem(R.string.settings_background_updates_section_general)

        sectionHeaderItem(R.string.settings_background_updates_section_troubleshoot)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            clickablePreferenceItem(R.string.settings_background_updates_battery_optimization) { id ->
                PreferenceView(
                    titleId = id,
                    summaryId = R.string.settings_background_updates_battery_optimization_summary
                ) {
                    val packageName: String = context.packageName
                    if (!context.powerManager.isIgnoringBatteryOptimizations(packageName)) {
                        try {
                            @SuppressLint("BatteryLife")
                            val intent = Intent().apply {
                                action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                                data = "package:$packageName".toUri()
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            SnackbarHelper.showSnackbar(context.getString(R.string.settings_background_updates_battery_optimization_activity_not_found))
                        }
                    } else {
                        SnackbarHelper.showSnackbar(context.getString(R.string.settings_background_updates_battery_optimization_disabled))
                    }
                }
            }
        }
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
                                showDisableNotificationChannelDialog(context)
                            }
                        }
                    } else {
                        PollingManager.resetNormalBackgroundTask(context, false)
                    }
                },
            )
        }
        clickablePreferenceItem(R.string.settings_background_updates_dont_kill_my_app_title) { id ->
            PreferenceView(
                titleId = id,
                summaryId = R.string.settings_background_updates_dont_kill_my_app_summary
            ) {
                uriHandler.openUri("https://dontkillmyapp.com/")
            }
        }
        clickablePreferenceItem(R.string.settings_background_updates_worker_info_title) { id ->
            PreferenceView(
                titleId = id
            ) {
                context.startActivity(Intent(context, WorkerInfoActivity::class.java))
            }
        }
        sectionFooterItem(R.string.settings_background_updates_section_troubleshoot)

        bottomInsetItem()
    }
}

@RequiresApi(api = Build.VERSION_CODES.O)
private fun showDisableNotificationChannelDialog(context: Context) {
    MaterialAlertDialogBuilder(context)
        .setTitle(R.string.settings_background_updates_method_notification_dialog_title)
        .setMessage(context.getString(R.string.settings_background_updates_method_notification_dialog_content).replace("$", context.getString(R.string.notification_channel_background_services)))
        .setPositiveButton(R.string.action_continue) { _, _ ->
            val intent = Intent().apply {
                action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
            context.startActivity(intent)
        }
        .setCancelable(true)
        .show()
}
